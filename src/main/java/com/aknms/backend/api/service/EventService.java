package com.aknms.backend.api.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.aknms.backend.api.Events;
import com.aknms.backend.api.model.Event;
import com.aknms.backend.api.model.EventType;
import com.aknms.backend.api.model.EventTypeCount;
import com.aknms.backend.api.model.User;
import com.aknms.backend.api.repo.EventRepo;

@Service
public class EventService implements Events {

	@Autowired
	private EventRepo eventRepo;

	@Autowired
	private UserService userService;

	@Override
	public Event getEventById(long id) {
		Event event = eventRepo.findById(id);
		return event;
	}

	@Override
	public Iterable<Event> getAllEvents() {
		Iterable<Event> events = eventRepo.findAll();
		return events;
	}

	@Override
	public List<EventTypeCount> getEventCount() {
		List<EventTypeCount> eventTypeCounts = eventRepo.countByEventType();
		// Commenting creation of Total Events Count as react-minimal-pie doesn't need
		// it
		/*
		 * Long count = eventRepo.count(); eventTypeCounts.add(new
		 * EventTypeCount(count));
		 */
		return eventTypeCounts;
	}

	@Override
	public List<EventTypeCount> getEventCountFilterUser(String username) {
		User user = userService.getUser(username);
		List<String> ipAddressList = userService.getIpAddressListFromUser(user);
		List<EventTypeCount> eventTypeCounts = new ArrayList<EventTypeCount>();
		if (CollectionUtils.isEmpty(ipAddressList)) {
			eventTypeCounts = eventRepo.countByEventType();
		} else {
			eventTypeCounts = eventRepo.countByEventTypeFilterIp(ipAddressList);
		}
		return eventTypeCounts;
	}

	@Override
	public List<Event> getEventsLastSince(int lastSinceInMinutes, Integer fromRecordId, Integer recordCount,
			String column, String direction) {
		System.out.println(Calendar.getInstance().getTimeInMillis());
		Pageable getRecords = PageRequest.of(fromRecordId, recordCount,
				Sort.by(Direction.valueOf(direction.toUpperCase()), column));

		List<Event> events = eventRepo.findByLastUpdatedInDate(
				Calendar.getInstance().getTimeInMillis() - lastSinceInMinutes * 60 * 1000, getRecords);
		return events;
	}

	@Override
	public List<Event> getEventsByDeviceIP(String ipAddress, Integer fromRecordId, Integer recordCount, String column,
			String direction) throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getByName(ipAddress);
		Pageable getRecords = PageRequest.of(fromRecordId, recordCount,
				Sort.by(Direction.valueOf(direction.toUpperCase()), column));

		List<Event> events = eventRepo.findAllByIpAddress(ipAddress, getRecords);
		return events;
	}

	@Override
	public List<Event> getEventByType(String eventType, Integer fromRecordId, Integer recordCount, String column,
			String direction) {
		Pageable getRecords = PageRequest.of(fromRecordId, recordCount,
				Sort.by(Direction.valueOf(direction.toUpperCase()), column));
		List<Event> events = eventRepo.findByType(EventType.valueOf(eventType), getRecords);
		return events;
	}

	@Override
	public List<Event> getNEventsFromRecordIdFilterUser(Integer fromRecordId, Integer recordCount, String username,
			String column, String direction) {
		User user = userService.getUser(username);
		List<String> ipAddressList = userService.getIpAddressListFromUser(user);

		List<Event> events = new ArrayList<Event>();
		Pageable getRecords = PageRequest.of(fromRecordId, recordCount,
				Sort.by(Direction.valueOf(direction.toUpperCase()), column));
		if (CollectionUtils.isEmpty(ipAddressList)) {
			events = eventRepo.findNEventsFromId(getRecords);
		} else {
			events = eventRepo.findByIpAddressIn(ipAddressList, getRecords);
		}
		return events;
	}

	@Override
	public List<Event> getNEventsFromRecordId(Integer fromRecordId, Integer recordCount, String column,
			String direction) {
		Pageable getRecords = PageRequest.of(fromRecordId, recordCount,
				Sort.by(Direction.valueOf(direction.toUpperCase()), column));
		List<Event> events = eventRepo.findNEventsFromId(getRecords);
		return events;
	}

	/*
	 * Unused Method
	 */
	/*
	 * private String getIpAddressListForQuery(List<String> ipAddressList) { if
	 * (CollectionUtils.isEmpty(ipAddressList)) { return ""; } String
	 * commaSeparatedIpAddressList = ""; for (String ipAddress : ipAddressList) {
	 * commaSeparatedIpAddressList =
	 * commaSeparatedIpAddressList.concat("'").concat(ipAddress).concat("'")
	 * .concat(","); } commaSeparatedIpAddressList =
	 * commaSeparatedIpAddressList.substring(0, commaSeparatedIpAddressList.length()
	 * - 1); return commaSeparatedIpAddressList; }
	 */

}
