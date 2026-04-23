package io.example.application;

import akka.javasdk.agent.Agent;
import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.FunctionTool;

@Component(id = "flight-conditions-agent")
public class FlightConditionsAgent extends Agent {

    public record ConditionsReport(String timeSlotId, Boolean meetsRequirements) {
    }

    private static final String SYSTEM_MESSAGE = """
            You are a flight dispatcher responsible for evaluating weather conditions for VFR (Visual Flight Rules) flight operations.

            VFR minimum requirements:
            - Visibility: at least 3 statute miles
            - Ceiling: at least 1,000 feet AGL (Above Ground Level)
            - Wind speed: no more than 25 knots
            - No active thunderstorms or severe weather

            When asked to evaluate a time slot:
            1. Call the getWeatherForecast tool with the provided time slot ID to retrieve the forecast.
            2. Compare each forecasted condition against the VFR minimums above.
            3. If the slot is more than 10 days in the future and no reliable forecast is available, conditionally approve with meetsRequirements=true.
            4. Set meetsRequirements to true ONLY if ALL criteria are met. Set it to false if ANY single criterion is not met.
            5. Return your answer as a JSON object in EXACTLY this format (no markdown, no code fences, no extra text):
               {"timeSlotId": "<the slotId you were given>", "meetsRequirements": <true or false>}
            """.stripIndent();

    public Effect<ConditionsReport> query(String timeSlotId) {
        return effects()
                .systemMessage(SYSTEM_MESSAGE)
                .userMessage(timeSlotId)
                .responseAs(ConditionsReport.class)
                .thenReply();
    }

    @FunctionTool(description = "Queries the weather conditions as they are forecasted based on the time slot ID of the training session booking. The time slot ID has the format YYYY-MM-DD-HH.")
    private String getWeatherForecast(String timeSlotId) {
        // SlotId format: YYYY-MM-DD-HH (e.g. 2025-12-10-14)
        // Day 13 of any month simulates below-VFR conditions for testing
        String[] parts = timeSlotId.split("-");
        boolean badWeather = parts.length >= 3 && "13".equals(parts[2]);

        if (badWeather) {
            return "Weather forecast for slot " + timeSlotId + ": "
                    + "Visibility 1 statute mile (below VFR minimum of 3 sm), "
                    + "ceiling 400 feet AGL (below VFR minimum of 1000 ft), "
                    + "wind speed 35 knots (above VFR maximum of 25 kt). "
                    + "Thunderstorm activity reported in the area. "
                    + "Conditions do NOT meet VFR requirements.";
        } else {
            return "Weather forecast for slot " + timeSlotId + ": "
                    + "Visibility 10 statute miles (above VFR minimum of 3 sm), "
                    + "ceiling 5000 feet AGL (above VFR minimum of 1000 ft), "
                    + "wind speed 8 knots (below VFR maximum of 25 kt). "
                    + "No severe weather reported. "
                    + "Conditions meet VFR requirements.";
        }
    }
}
