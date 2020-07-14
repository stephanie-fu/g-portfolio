// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Base cases
    if (request.getAttendees().isEmpty()) { return new ArrayList<>(Arrays.asList(TimeRange.WHOLE_DAY)); }
    if (request.getDuration() >= TimeRange.WHOLE_DAY.duration()) { return new ArrayList<>(); }
    
    // Preprocess event input
    List<Event> pertinentEvents = new ArrayList<>(filterEvents(new ArrayList<>(events), request.getAttendees()));
    Collections.sort(pertinentEvents, (Event e1, Event e2) -> TimeRange.ORDER_BY_END.compare(e1.getWhen(), e2.getWhen()));
    
    List<TimeRange> openSlots = new ArrayList<>();
    int previousEnd = 0;

    for (Event event : pertinentEvents) {
      TimeRange eventTimeRange = event.getWhen();
      if (eventTimeRange.start() - previousEnd >= request.getDuration()) {
        openSlots.add(TimeRange.fromStartEnd(previousEnd, eventTimeRange.start(), false));
      }
      previousEnd = eventTimeRange.end();
    }
    if ((previousEnd < TimeRange.END_OF_DAY) && (TimeRange.END_OF_DAY - previousEnd >= request.getDuration())) {
      openSlots.add(TimeRange.fromStartEnd(previousEnd, TimeRange.END_OF_DAY, true));
    }
    return openSlots;
  }

  private static List<Event> filterEventsByAttendees(List<Event> events, Collection<String> attendees) {
    List<Event> eventsWithAppropriateAttendees = new ArrayList<>();
    Collection<String> eventAttendees;
    for (Event event : events) {
      eventAttendees = new HashSet<>(event.getAttendees());
      eventAttendees.retainAll(attendees);
      // Check size of intersection calculated above
      if (eventAttendees.size() > 0) { eventsWithAppropriateAttendees.add(event); }
    }
    return eventsWithAppropriateAttendees;
  }

  private static List<Event> filterEventsByContain(List<Event> events) {
    List<Event> nonOverlappedEvents = new ArrayList<>();
    boolean eventContainsAnother;
    for (Event event1 : events) {
      eventContainsAnother = false;
      for (Event event2 : events) {
        // Invalidates event1 if it is contained by event2
        if (event1 != event2) { eventContainsAnother = event2.getWhen().contains(event1.getWhen()); }
      }
      if (!eventContainsAnother) { nonOverlappedEvents.add(event1); }
    }
    return nonOverlappedEvents;
  }

  private static List<Event> filterEvents(List<Event> events, Collection<String> attendees) {
    return filterEventsByAttendees(filterEventsByContain(events), attendees);
  }
}
