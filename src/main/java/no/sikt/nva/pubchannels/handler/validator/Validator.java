package no.sikt.nva.pubchannels.handler.validator;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;

import java.time.Year;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import nva.commons.core.JacocoGenerated;
import org.apache.commons.validator.routines.ISSNValidator;
import org.apache.commons.validator.routines.UrlValidator;

public final class Validator {

  public static final String ISBN_PREFIX_PATTERN = "^(?:97(8|9)-)?[0-9]{1,5}-[0-9]{1,7}$";
  public static final int MAX_LENGTH_ISBN_PREFIX = 13;
  private static final String IS_REQUIRED_STRING = "%s is required.";

  @JacocoGenerated
  private Validator() {}

  public static void validateString(String value, int minLength, int maxLength, String name) {
    Objects.requireNonNull(value, format(IS_REQUIRED_STRING, name));
    if (value.length() < minLength) {
      throw new ValidationException(
          format("%s is too short. Minimum length is %d", name, minLength));
    }
    if (value.length() > maxLength) {
      throw new ValidationException(
          format("%s is too long. Maximum length is %d", name, maxLength));
    }
  }

  public static void validateOptionalIsbnPrefix(String isbnPrefix, String name) {
    if (nonNull(isbnPrefix) && isbnPrefix.length() > MAX_LENGTH_ISBN_PREFIX) {
      throw new ValidationException(
          format("%s is too long. Maximum length is %d", name, MAX_LENGTH_ISBN_PREFIX));
    }
    if (nonNull(isbnPrefix) && !Pattern.matches(ISBN_PREFIX_PATTERN, isbnPrefix)) {
      throw new ValidationException(format("%s has an invalid format.", name));
    }
  }

  public static void validateOptionalIssn(String issn, String name) {
    if (issn != null && !ISSNValidator.getInstance().isValid(issn.trim())) {
      throw new ValidationException(format("%s has an invalid ISSN format.", name));
    }
  }

  public static void validateOptionalUrl(String url, String name) {
    if (url != null && !UrlValidator.getInstance().isValid(url)) {
      throw new ValidationException(format("%s has an invalid URL format", name));
    }
  }

  public static void validateUuid(String value, String name) {
    Objects.requireNonNull(value, format(IS_REQUIRED_STRING, name));
    attempt(() -> UUID.fromString(value))
        .orElseThrow(
            failure -> new ValidationException(format("%s has an invalid UUIDv4 format", name)));
  }

  public static void validateYear(String value, Year minAcceptableYear, String name) {
    if (value != null) {
      var year =
          attempt(() -> Year.parse(value))
              .orElseThrow(
                  failure ->
                      new ValidationException(format("%s field is not a valid year.", name)));
      var now = Year.now();
      if (year.isBefore(minAcceptableYear) || year.isAfter(now.plusYears(1))) {
        throw new ValidationException(
            format(
                "%s is not between the year %d and %d",
                name, minAcceptableYear.getValue(), now.getValue()));
      }
    }
  }

  public static void validatePagination(int offset, int size) {
    if (offset % size != 0) {
      throw new ValidationException("Offset needs to be divisible by size");
    }
  }
}
