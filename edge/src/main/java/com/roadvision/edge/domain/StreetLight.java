package com.roadvision.edge.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Дані датчика вуличного освітлення: рівень освітленості (люкс) + стан ліхтаря. */
public record StreetLight(double lux, @JsonProperty("is_on") boolean isOn) {
}
