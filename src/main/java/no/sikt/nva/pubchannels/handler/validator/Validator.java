package no.sikt.nva.pubchannels.handler.validator;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;

import java.time.Year;
import java.util.Objects;
import java.util.regex.Pattern;
import no.sikt.nva.pubchannels.handler.fetch.RequestObject;
import org.apache.commons.validator.routines.ISSNValidator;
import org.apache.commons.validator.routines.UrlValidator;

public class Validator {

  public static final String ISBN_PREFIX_PATTERN = "^(?:97(8|9)-)?[0-9]{1,5}-[0-9]{1,7}$";
  public static final int MAX_LENGTH_ISBN_PREFIX = 13;
  private static final String IS_REQUIRED_STRING = "%s is required.";
  protected static final Year MIN_ACCEPTABLE_YEAR = Year.of(1900);
  protected static final Year MAX_ACCEPTABLE_YEAR = Year.of(2100);

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

  public static void validateYear(String value) {
    if (value != null) {
      var year =
          attempt(() -> Year.parse(value))
              .orElseThrow(
                  failure ->
                      new ValidationException("Invalid year: Failed to parse year parameter"));
      if (year.isBefore(MIN_ACCEPTABLE_YEAR) || year.isAfter(MAX_ACCEPTABLE_YEAR)) {
        throw new ValidationException(
            format(
                "Invalid year: Must be in range %d to %d",
                MIN_ACCEPTABLE_YEAR.getValue(), MAX_ACCEPTABLE_YEAR.getValue()));
      }
    }
  }

  public static void validatePagination(int offset, int size) {
    if (offset % size != 0) {
      throw new ValidationException("Offset needs to be divisible by size");
    }
  }

  public void validate(RequestObject requestObject) {
    requestObject.getYear().ifPresent(Validator::validateYear);
  }
}
