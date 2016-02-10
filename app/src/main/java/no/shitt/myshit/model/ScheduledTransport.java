package no.shitt.myshit.model;

import org.json.JSONObject;

import java.text.DateFormat;

import no.shitt.myshit.helper.StringUtil;

public class ScheduledTransport extends GenericTransport {
    // MARK: Properties
    // No additional properties needed

    @Override
    public String getStartInfo() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendWithLeadingSeparator(sb, startTime(null, DateFormat.SHORT), "", false);
        StringUtil.appendWithLeadingSeparator(sb, departureStop, ": ", false);
        StringUtil.appendWithLeadingSeparator(sb, departureLocation, ", ", false);
        return sb.toString();
    }
    @Override
    public String getEndInfo() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendWithLeadingSeparator(sb, endTime(null, DateFormat.SHORT), "", false);
        StringUtil.appendWithLeadingSeparator(sb, arrivalStop, ": ", false);
        StringUtil.appendWithLeadingSeparator(sb, arrivalLocation, ", ", false);
        return sb.toString();
    }

    ScheduledTransport(int tripId, String tripCode, JSONObject elementData) {
        super(tripId, tripCode, elementData);
    }

    // MARK: Methods
}
