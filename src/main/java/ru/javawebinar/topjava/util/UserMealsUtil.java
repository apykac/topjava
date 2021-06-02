package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        if (meals == null || meals.isEmpty()) {
            return new ArrayList<>(1);
        }
        List<UserMealWithExcess> result = new ArrayList<>(Math.min(10, meals.size()));
        AtomicBoolean excess = new AtomicBoolean(false);
        int totalDayCalories = 0;
        for (UserMeal userMeal : meals) {
            LocalDateTime dateTime = userMeal.getDateTime();
            if (dateTime != null && isBetween(dateTime, startTime, endTime)) {
                totalDayCalories += userMeal.getCalories();
                if (totalDayCalories > caloriesPerDay) {
                    excess.set(true);
                }
                result.add(toUserMealWithExcess(userMeal, excess));
            }
        }

        return result;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        if (meals == null || meals.isEmpty()) {
            return new ArrayList<>(1);
        }

        AtomicBoolean excess = new AtomicBoolean(false);
        AtomicInteger totalDayCalories = new AtomicInteger(0);
        return meals.stream()
                .filter(userMeal -> userMeal.getDateTime() != null)
                .filter(userMeal -> isBetween(userMeal.getDateTime(), startTime, endTime))
                .map(userMeal -> {
                    totalDayCalories.addAndGet(userMeal.getCalories());
                    if (totalDayCalories.get() > caloriesPerDay) {
                        excess.set(true);
                    }
                    return toUserMealWithExcess(userMeal, excess);
                })
                .collect(Collectors.toList());
    }

    private static boolean isBetween(LocalDateTime target, LocalTime startTime, LocalTime endTime) {
        return isAfter(target, startTime) && isBefore(target, endTime);
    }

    private static boolean isAfter(LocalDateTime target, LocalTime startTime) {
        return startTime == null || target.toLocalTime().isAfter(startTime);
    }

    private static boolean isBefore(LocalDateTime target, LocalTime endTime) {
        return endTime == null || target.toLocalTime().isBefore(endTime);
    }

    private static UserMealWithExcess toUserMealWithExcess(UserMeal userMeal, AtomicBoolean excess) {
        return new UserMealWithExcess(
                userMeal.getDateTime(),
                userMeal.getDescription(),
                userMeal.getCalories(),
                excess
        );
    }
}
