/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package main.java.hale.rules;

import java.text.NumberFormat;

import main.java.hale.Game;

public class Date
{
    private final NumberFormat numberFormat;

    public int roundMillis;

    private int roundNumber;

    private int rounds, minutes, hours, days, months;

    public int ROUNDS_PER_MINUTE;
    public int MINUTES_PER_HOUR;
    public int HOURS_PER_DAY;
    public int DAYS_PER_MONTH;

    public int ROUNDS_PER_HOUR;
    public int ROUNDS_PER_DAY;
    public int ROUNDS_PER_MONTH;
    public int MINUTES_PER_DAY;
    public int MINUTES_PER_MONTH;
    public int HOURS_PER_MONTH;

    public Date(int roundsPerMinute, int minutesPerHour, int hoursPerDay, int daysPerMonth)
    {
        ROUNDS_PER_MINUTE = roundsPerMinute;
        MINUTES_PER_HOUR = minutesPerHour;
        HOURS_PER_DAY = hoursPerDay;
        DAYS_PER_MONTH = daysPerMonth;

        computeTimeScales();

        roundNumber = 0;
        rounds = 1;
        minutes = 0;
        hours = 0;
        days = 0;
        months = 0;

        this.numberFormat = NumberFormat.getInstance();
        this.numberFormat.setMinimumIntegerDigits(2);
    }

    private void computeTimeScales()
    {
        ROUNDS_PER_HOUR = ROUNDS_PER_MINUTE * MINUTES_PER_HOUR;
        ROUNDS_PER_DAY = ROUNDS_PER_HOUR * HOURS_PER_DAY;
        ROUNDS_PER_MONTH = ROUNDS_PER_DAY * DAYS_PER_MONTH;
        MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
        MINUTES_PER_MONTH = MINUTES_PER_DAY * DAYS_PER_MONTH;
        HOURS_PER_MONTH = HOURS_PER_DAY * DAYS_PER_MONTH;

        roundMillis = 1000 * 60 / ROUNDS_PER_MINUTE;
    }

    public void setRoundsPerMinute(int roundsPerMinute)
    {
        ROUNDS_PER_MINUTE = roundsPerMinute;
        computeTimeScales();
    }

    public void setMinutesPerHour(int minutesPerHour)
    {
        MINUTES_PER_HOUR = minutesPerHour;
        computeTimeScales();
    }

    public void setHoursPerDay(int hoursPerDay)
    {
        HOURS_PER_DAY = hoursPerDay;
        computeTimeScales();
    }

    public void setDaysPerMonth(int daysPerMonth)
    {
        DAYS_PER_MONTH = daysPerMonth;
        computeTimeScales();
    }

    public String getDateString(int months, int days, int hours, int minutes, int rounds)
    {
        int roundNumber = months * ROUNDS_PER_MONTH + days * ROUNDS_PER_DAY + hours * ROUNDS_PER_HOUR +
                minutes * ROUNDS_PER_MINUTE + rounds;

        rounds = roundNumber % ROUNDS_PER_MINUTE;
        roundNumber -= rounds;

        minutes = (roundNumber % ROUNDS_PER_HOUR) / ROUNDS_PER_MINUTE;
        roundNumber -= minutes * ROUNDS_PER_MINUTE;

        hours = (roundNumber % ROUNDS_PER_DAY) / ROUNDS_PER_HOUR;
        roundNumber -= hours * ROUNDS_PER_HOUR;

        days = (roundNumber % ROUNDS_PER_MONTH) / ROUNDS_PER_DAY;
        roundNumber -= days * ROUNDS_PER_DAY;

        months = (roundNumber / ROUNDS_PER_MONTH);

        return shortString(months, days, hours, minutes, rounds);
    }

    private void recalculate()
    {
        int roundCounter = roundNumber;

        rounds = roundNumber % ROUNDS_PER_MINUTE;
        roundCounter -= rounds;

        minutes = (roundCounter % ROUNDS_PER_HOUR) / ROUNDS_PER_MINUTE;
        roundCounter -= minutes * ROUNDS_PER_MINUTE;

        hours = (roundCounter % ROUNDS_PER_DAY) / ROUNDS_PER_HOUR;
        roundCounter -= hours * ROUNDS_PER_HOUR;

        days = (roundCounter % ROUNDS_PER_MONTH) / ROUNDS_PER_DAY;
        roundCounter -= days * ROUNDS_PER_DAY;

        months = (roundCounter / ROUNDS_PER_MONTH);

        Game.curCampaign.checkEncounterRespawns();
    }

    public void reset()
    {
        roundNumber = 0;

        recalculate();
    }

    public void incrementRounds(int rounds)
    {
        roundNumber += rounds;
        recalculate();
    }

    public void incrementMinutes(int minutes)
    {
        roundNumber += minutes * ROUNDS_PER_MINUTE;
        recalculate();
    }

    public void incrementHours(int hours)
    {
        roundNumber += hours * ROUNDS_PER_HOUR;
        recalculate();
    }

    public void incrementDays(int days)
    {
        roundNumber += days * ROUNDS_PER_DAY;
        recalculate();
    }

    public void incrementMonths(int months)
    {
        roundNumber += months * ROUNDS_PER_MONTH;
        recalculate();
    }

    public void incrementRound()
    {
        incrementRounds(1);
    }

    public void incrementMinute()
    {
        incrementMinutes(1);
    }

    public void incrementHour()
    {
        incrementHours(1);
    }

    public void incrementDay()
    {
        incrementDays(1);
    }

    public void incrementMonth()
    {
        incrementMonths(1);
    }

    public int getTotalRoundsElapsed()
    {
        return roundNumber;
    }

    public int getRounds()
    {
        return rounds;
    }

    public int getMinutes()
    {
        return minutes;
    }

    public int getHours()
    {
        return hours;
    }

    public int getDays()
    {
        return days;
    }

    public int getMonths()
    {
        return months;
    }

    private String shortString(int months, int days, int hours, int minutes, int rounds)
    {
        StringBuilder sb = new StringBuilder();

        if (months != 0) {
            sb.append(months);
            if (months == 1) {
                sb.append(" Month ");
            } else {
                sb.append(" Months ");
            }
        }

        if (days != 0) {
            sb.append(days);
            if (days == 1) {
                sb.append(" Day ");
            } else {
                sb.append(" Days ");
            }
        }

        if (hours != 0) {
            sb.append(hours);
            if (hours == 1) {
                sb.append(" Hour ");
            } else {
                sb.append(" Hours ");
            }
        }

        if (minutes != 0) {
            sb.append(minutes);
            if (minutes == 1) {
                sb.append(" Minute ");
            } else {
                sb.append(" Minutes ");
            }
        }

        if (rounds != 0) {
            sb.append(rounds);
            if (rounds == 1) {
                sb.append(" Round ");
            } else {
                sb.append(" Rounds ");
            }
        }

        if (sb.length() == 0) sb.append("0 Minutes");

        return sb.toString().trim();
    }

    public String monthDayTimeString()
    {
        return numberFormat.format(hours) + ":" + numberFormat.format(minutes) + " Day " + (days + 1) + ", Month " + (months + 1);
    }

    @Override
    public String toString()
    {
        return "Month " + months + ", Day " + days + ", Hour " + hours + ", Minute " + minutes + ", Round " + rounds;
    }
}
