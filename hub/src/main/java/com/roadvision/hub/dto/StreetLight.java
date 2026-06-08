package com.roadvision.hub.dto;

/** Дані датчика вуличного освітлення: освітленість (люкс) + стан ліхтаря (is_on). */
public record StreetLight(double lux, boolean isOn) {
}
