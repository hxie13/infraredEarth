package cn.ac.sitp.infrared.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class RequestValueUtils {

    public static final String ILLEGAL_PARAMS_MESSAGE = "Illegal Params";
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    private RequestValueUtils() {
    }

    public static int normalizePage(Integer currPage) {
        if (currPage == null || currPage < 1) {
            return DEFAULT_PAGE;
        }
        return currPage;
    }

    public static int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public static Date parseBeginDate(String value) {
        return parseDate(value, " 00:00:00");
    }

    public static Date parseEndDate(String value) {
        return parseDate(value, " 23:59:59");
    }

    public static void validateDateRange(Date beginDate, Date endDate) {
        if (beginDate != null && endDate != null && beginDate.after(endDate)) {
            throw new IllegalArgumentException(ILLEGAL_PARAMS_MESSAGE);
        }
    }

    public static Long requirePositiveId(Long value) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(ILLEGAL_PARAMS_MESSAGE);
        }
        return value;
    }

    public static Integer requirePositiveInt(Integer value) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(ILLEGAL_PARAMS_MESSAGE);
        }
        return value;
    }

    public static List<Long> parsePositiveLongList(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(ILLEGAL_PARAMS_MESSAGE);
        }
        List<Long> result = new ArrayList<>();
        String[] values = value.split(",");
        for (String item : values) {
            String trimmed = StringUtils.trimToNull(item);
            if (trimmed == null) {
                continue;
            }
            try {
                long parsed = Long.parseLong(trimmed);
                if (parsed < 1) {
                    throw new IllegalArgumentException(ILLEGAL_PARAMS_MESSAGE);
                }
                result.add(parsed);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(ILLEGAL_PARAMS_MESSAGE, e);
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException(ILLEGAL_PARAMS_MESSAGE);
        }
        return result;
    }

    public static String trimToNull(String value) {
        return StringUtils.trimToNull(value);
    }

    private static Date parseDate(String value, String suffix) {
        String trimmedValue = StringUtils.trimToNull(value);
        if (trimmedValue == null) {
            return null;
        }
        Date parsedDate = Util.strToDate(trimmedValue + suffix, Util.FORMAT_LONG);
        if (parsedDate == null) {
            throw new IllegalArgumentException(ILLEGAL_PARAMS_MESSAGE);
        }
        return parsedDate;
    }
}
