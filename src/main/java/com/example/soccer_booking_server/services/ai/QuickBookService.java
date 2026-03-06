package com.example.soccer_booking_server.services.ai;

import com.example.soccer_booking_server.dto.ai.AiPreferencesResponse;
import com.example.soccer_booking_server.dto.ai.AiRecommendResponse;
import com.example.soccer_booking_server.dto.ai.QuickBookOptionDTO;
import com.example.soccer_booking_server.dto.BookingHistoryItemDTO;
import com.example.soccer_booking_server.entitis.Field;
import com.example.soccer_booking_server.entitis.FieldSlot;
import com.example.soccer_booking_server.enums.BookingStatus;
import com.example.soccer_booking_server.enums.FieldStatus;
import com.example.soccer_booking_server.repository.BookingRepository;
import com.example.soccer_booking_server.repository.FieldRepository;
import com.example.soccer_booking_server.repository.FieldSlotRepository;
import com.example.soccer_booking_server.services.BookingAppService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuickBookService {

    private final BookingAppService bookingAppService;
    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final FieldSlotRepository fieldSlotRepository;
    private final AiRecommenderClient aiClient;

    // Java DayOfWeek: Mon=1..Sun=7
    // Python weekday: Mon=0..Sun=6
    private int toPythonWeekday(LocalDate d) {
        return d.getDayOfWeek().getValue() % 7; // Sun -> 0? actually 7%7=0 => Sun=0 (Python wants 6)
        // Wait: Java Sun=7 -> 0; Python Sun=6. So we need mapping:
        // Let's implement properly below.
    }

    private int javaToPythonWeekday(LocalDate d) {
        // Java: Mon=1..Sun=7
        int j = d.getDayOfWeek().getValue();
        // Python: Mon=0..Sun=6
        return j - 1;
    }

    public List<QuickBookOptionDTO> quickBook(Integer userId, Integer days, Integer topK) {

        // 1) Lịch sử tốt để học "gu"
        List<BookingStatus> goodStatuses = List.of(
                BookingStatus.DEPOSITED,
                BookingStatus.CHECKED_IN,
                BookingStatus.COMPLETED
        );

        var page = bookingAppService.getUserHistory(userId, 0, 200, goodStatuses);
        List<BookingHistoryItemDTO> history = page.getContent();
        if (history == null || history.isEmpty()) {
            return Collections.emptyList(); // MVP: user mới chưa có lịch sử
        }

        // 2) Payload lịch sử cho AI
        List<Map<String, Object>> userBookings = history.stream().map(h -> {
            Map<String, Object> m = new HashMap<>();
            m.put("user_id", userId);
            m.put("booking_date", h.getBookingDate().toString());
            m.put("slot_id", h.getSlotId());
            m.put("field_id", h.getFieldId());
            return m;
        }).collect(Collectors.toList());

        // 3) Gọi AI lấy preferences
        AiPreferencesResponse prefs = aiClient.getPreferences(userId, userBookings);

        List<Integer> prefSlots = prefs.getSlots() != null ? prefs.getSlots() : List.of();
        List<Integer> prefFields = prefs.getFields() != null ? prefs.getFields() : List.of();
        Set<String> prefTypes = prefs.getField_types() != null ? new LinkedHashSet<>(prefs.getField_types()) : Set.of();
        Set<Integer> prefWeekdays = prefs.getWeekdays() != null ? new LinkedHashSet<>(prefs.getWeekdays()) : Set.of();

        // 4) Lấy fields ACTIVE
        List<Field> activeFields = fieldRepository.findAll().stream()
                .filter(f -> f.getStatus() == FieldStatus.ACTIVE)
                .collect(Collectors.toList());

        Map<Integer, Field> fieldMap = activeFields.stream()
                .collect(Collectors.toMap(Field::getFieldId, f -> f));

        // 5) Lấy slots và map slotId -> slot
        List<FieldSlot> allSlots = fieldSlotRepository.findAll();
        Map<Integer, FieldSlot> slotMap = allSlots.stream()
                .collect(Collectors.toMap(FieldSlot::getSlotId, s -> s));

        // 6) Generate dates (days tới)
        LocalDate start = LocalDate.now();
        List<LocalDate> dateRange = new ArrayList<>();
        for (int i = 0; i < days; i++) dateRange.add(start.plusDays(i));

        // Ưu tiên ngày theo weekday user hay đá (Python Mon=0..Sun=6)
        List<LocalDate> orderedDates = dateRange.stream()
                .sorted(Comparator.comparingInt(d -> prefWeekdays.contains(javaToPythonWeekday(d)) ? 0 : 1))
                .collect(Collectors.toList());

        // 7) Build candidates TRỐNG theo logic:
        // giữ slot/weekday → ưu tiên sân quen → nếu hết thì đổi sân khác
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> candidates = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        Map<String, Set<Integer>> occupiedCache = new HashMap<>();

        int targetCandidateCount = Math.max(topK * 6, 60); // tạo nhiều để AI rank

        for (LocalDate d : orderedDates) {

            // slot ưu tiên: prefSlots trước, fallback: slot còn lại
            List<Integer> slotOrder = new ArrayList<>(prefSlots);
            for (FieldSlot s : allSlots) {
                if (!slotOrder.contains(s.getSlotId())) slotOrder.add(s.getSlotId());
            }

            for (Integer slotId : slotOrder) {
                // field ưu tiên: prefFields trước
                List<Integer> fieldOrder = new ArrayList<>(prefFields);
                for (Field f : activeFields) {
                    if (!fieldOrder.contains(f.getFieldId())) fieldOrder.add(f.getFieldId());
                }

                for (Integer fieldId : fieldOrder) {
                    Field f = fieldMap.get(fieldId);
                    if (f == null) continue;

                    // (optional) ưu tiên type match (nhưng không chặn fallback)
                    boolean typePreferred = prefTypes.isEmpty() || prefTypes.contains(f.getType().name());

                    String cacheKey = fieldId + "_" + d;
                    Set<Integer> occ = occupiedCache.computeIfAbsent(cacheKey, k -> {
                        List<Integer> ids = bookingRepository.findOccupiedSlotIds(fieldId, d, now);
                        return new HashSet<>(ids);
                    });

                    if (occ.contains(slotId)) continue;

                    String uniq = fieldId + "|" + slotId + "|" + d;
                    if (!seen.add(uniq)) continue;

                    Map<String, Object> c = new HashMap<>();
                    c.put("booking_date", d.toString());
                    c.put("slot_id", slotId);
                    c.put("field_id", fieldId);
                    c.put("field_type", f.getType().name());
                    c.put("typePreferred", typePreferred);
                    candidates.add(c);

                    if (candidates.size() >= targetCandidateCount) break;
                }
                if (candidates.size() >= targetCandidateCount) break;
            }
            if (candidates.size() >= targetCandidateCount) break;
        }

        if (candidates.isEmpty()) return Collections.emptyList();

        // 8) Gọi AI /recommend để rank topK
        AiRecommendResponse rec = aiClient.recommend(userId, userBookings, candidates, topK);

        // 9) Enrich trả về FE: fieldName + slotStart/slotEnd/slotNumber
        List<QuickBookOptionDTO> out = new ArrayList<>();
        if (rec.getRecommendations() == null) return out;

        for (AiRecommendResponse.Recommendation r : rec.getRecommendations()) {
            Field f = fieldMap.get(r.getField_id());
            FieldSlot s = slotMap.get(r.getSlot_id());
            if (f == null || s == null) continue;

            out.add(QuickBookOptionDTO.builder()
                    .fieldId(f.getFieldId())
                    .fieldName(f.getFieldName())
                    .fieldType(f.getType())
                    .slotId(s.getSlotId())
                    .slotNumber(s.getSlotNumber())
                    .slotStart(s.getSlotStart())
                    .slotEnd(s.getSlotEnd())
                    .bookingDate(LocalDate.parse(r.getBooking_date()))
                    .score(r.getScore())
                    .build());
        }

        return out;
    }
}