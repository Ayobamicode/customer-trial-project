package edu.hcc.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record CustomerInput(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 150) String addressLine1,
        @Size(max = 150) String addressLine2,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Pattern(regexp = "[A-Z]{2}") String state,
        @NotBlank @Pattern(regexp = "[0-9]{5}(-[0-9]{4})?") String zip) {

    public CustomerInput {
        name = trim(name);
        addressLine1 = trim(addressLine1);
        addressLine2 = addressLine2 == null ? "" : addressLine2.strip();
        city = trim(city);
        state = state == null ? null : state.strip().toUpperCase(Locale.ROOT);
        zip = trim(zip);
    }

    private static String trim(String value) {
        return value == null ? null : value.strip();
    }
}
