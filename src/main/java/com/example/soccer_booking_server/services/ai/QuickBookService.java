package com.example.soccer_booking_server.services.ai;

import com.example.soccer_booking_server.dto.BookingHistoryItemDTO;
import com.example.soccer_booking_server.dto.ai.AiPreferencesResponse;
import com.example.soccer_booking_server.dto.ai.AiRecommendResponse;
import com.example.soccer_booking_server.dto.ai.QuickBookOptionDTO;
import com.example.soccer_booking_server.entitis.Field;
import com.example.soccer_booking_server.entitis.FieldSlot;
import com.example.soccer_booking_server.enums.BookingStatus;
import com.example.soccer_booking_server.enums.FieldStatus;
import com.example.soccer_booking_server.repository.BookingRepository;
import com.example.soccer_booking_server.repository.FieldRepository;
import com.example.soccer_booking_server.repository.FieldSlotRepository;
import com.example.soccer_booking_server.services.BookingAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuickBookService {

    private final BookingAppService bookingAppService;
    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final FieldSlotRepository fieldSlotRepository;
    private final AiRecommenderClient aiClient;

    private int javaToPythonWeekday(LocalDate d) {
        return d.getDayOfWeek().getValue() - 1;
    }

    public List<QuickBookOptionDTO> quickBook(Integer userId, Integer days, Integer topK) {

        try {

            System.out.println("\n========== QUICK BOOK DEBUG ==========");

            List<BookingStatus> goodStatuses = List.of(
                    BookingStatus.DEPOSITED,
                    BookingStatus.CHECKED_IN,
                    BookingStatus.COMPLETED
            );

            var page = bookingAppService.getUserHistory(userId, 0, 200, goodStatuses);
            List<BookingHistoryItemDTO> history = page.getContent();

            System.out.println("history size = " + history.size());

            if (history.isEmpty()) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> userBookings = history.stream().map(h -> {
                Map<String, Object> m = new HashMap<>();
                m.put("user_id", userId);
                m.put("booking_date", h.getBookingDate().toString());
                m.put("slot_id", h.getSlotId());
                m.put("field_id", h.getFieldId());
                return m;
            }).collect(Collectors.toList());

            AiPreferencesResponse prefs = aiClient.getPreferences(userId, userBookings);

            System.out.println("AI preferences slots = " + prefs.getSlots());
            System.out.println("AI preferences fields = " + prefs.getFields());
            System.out.println("AI preferences weekdays = " + prefs.getWeekdays());

            List<Integer> prefSlots = prefs.getSlots() != null ? prefs.getSlots() : List.of();
            List<Integer> prefFields = prefs.getFields() != null ? prefs.getFields() : List.of();
            Set<Integer> prefWeekdays = prefs.getWeekdays() != null ?
                    new LinkedHashSet<>(prefs.getWeekdays()) : Set.of();

            List<Field> fields = fieldRepository.findAll().stream()
                    .filter(f -> f.getStatus() == null || f.getStatus() == FieldStatus.ACTIVE)
                    .collect(Collectors.toList());

            List<FieldSlot> allSlots = fieldSlotRepository.findAll();

            Map<Integer, Field> fieldMap =
                    fields.stream().collect(Collectors.toMap(Field::getFieldId, f -> f));

            Map<Integer, FieldSlot> slotMap =
                    allSlots.stream().collect(Collectors.toMap(FieldSlot::getSlotId, s -> s));

            LocalDate start = LocalDate.now();

            List<LocalDate> dateRange = new ArrayList<>();

            for (int i = 0; i < days; i++) {
                dateRange.add(start.plusDays(i));
            }

            List<LocalDate> orderedDates = dateRange.stream()
                    .sorted(Comparator.comparingInt(
                            d -> prefWeekdays.contains(javaToPythonWeekday(d)) ? 0 : 1
                    ))
                    .collect(Collectors.toList());

            LocalDateTime now = LocalDateTime.now();

            List<Map<String, Object>> candidates = new ArrayList<>();
            Set<String> seen = new HashSet<>();

            Map<String, Set<Integer>> occupiedCache = new HashMap<>();

            int targetCandidateCount = Math.max(topK * 6, 60);

            for (LocalDate d : orderedDates) {

                List<Integer> slotOrder = new ArrayList<>(prefSlots);

                for (FieldSlot s : allSlots) {
                    if (!slotOrder.contains(s.getSlotId())) {
                        slotOrder.add(s.getSlotId());
                    }
                }

                List<Integer> fieldOrder = new ArrayList<>(prefFields);

                for (Field f : fields) {
                    if (!fieldOrder.contains(f.getFieldId())) {
                        fieldOrder.add(f.getFieldId());
                    }
                }

                for (Integer slotId : slotOrder) {

                    for (Integer fieldId : fieldOrder) {

                        Field f = fieldMap.get(fieldId);

                        if (f == null) continue;

                        String cacheKey = fieldId + "_" + d;

                        Set<Integer> occ = occupiedCache.computeIfAbsent(cacheKey, k -> {
                            List<Integer> ids = bookingRepository
                                    .findOccupiedSlotIds(fieldId, d, now);
                            return new HashSet<>(ids);
                        });

                        if (occ.contains(slotId)) continue;

                        String uniq = fieldId + "|" + slotId + "|" + d;

                        if (!seen.add(uniq)) continue;

                        Map<String, Object> c = new HashMap<>();

                        c.put("booking_date", d.toString());
                        c.put("slot_id", slotId);
                        c.put("field_id", fieldId);

                        candidates.add(c);

                        if (candidates.size() >= targetCandidateCount) break;
                    }

                    if (candidates.size() >= targetCandidateCount) break;
                }

                if (candidates.size() >= targetCandidateCount) break;
            }

            System.out.println("candidates size = " + candidates.size());

            if (candidates.isEmpty()) {
                return Collections.emptyList();
            }

            AiRecommendResponse rec =
                    aiClient.recommend(userId, userBookings, candidates, topK);

            List<QuickBookOptionDTO> out = new ArrayList<>();

            // fallback nếu AI không trả kết quả
            if (rec == null || rec.getRecommendations() == null
                    || rec.getRecommendations().isEmpty()) {

                System.out.println("AI recommend empty -> fallback");

                Map<Integer, QuickBookOptionDTO> uniqueByField = new LinkedHashMap<>();
                Set<Integer> usedSlots = new HashSet<>();

                for (Map<String, Object> c : candidates) {

                    Integer fieldId = (Integer) c.get("field_id");
                    Integer slotId = (Integer) c.get("slot_id");
                    String bookingDate = (String) c.get("booking_date");

                    if (uniqueByField.containsKey(fieldId)) continue;

                    Field f = fieldMap.get(fieldId);
                    FieldSlot s = slotMap.get(slotId);

                    if (f == null || s == null) continue;

                    if (usedSlots.contains(slotId)
                            && usedSlots.size() < prefSlots.size()) {
                        continue;
                    }

                    QuickBookOptionDTO dto = QuickBookOptionDTO.builder()
                            .fieldId(f.getFieldId())
                            .fieldName(f.getFieldName())
                            .fieldType(f.getType())
                            .slotId(s.getSlotId())
                            .slotNumber(s.getSlotNumber())
                            .slotStart(s.getSlotStart())
                            .slotEnd(s.getSlotEnd())
                            .bookingDate(LocalDate.parse(bookingDate))
                            .score(0.5)
                            .build();

                    uniqueByField.put(fieldId, dto);
                    usedSlots.add(slotId);

                    if (uniqueByField.size() >= topK) break;
                }

                out = new ArrayList<>(uniqueByField.values());

                System.out.println("fallback result size = " + out.size());

                out.forEach(o -> System.out.println(
                        "suggest -> field=" + o.getFieldName()
                                + " slot=" + o.getSlotNumber()
                                + " date=" + o.getBookingDate()
                ));

                return out;
            }

            // nếu AI recommend OK
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

            System.out.println("AI recommend result size = " + out.size());

            return out;

        } catch (Exception e) {

            System.out.println("QUICK BOOK ERROR");

            e.printStackTrace();

            return Collections.emptyList();
        }
    }
}