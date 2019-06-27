package com.miamioh.ridesharing.app.data.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;
import lombok.Setter;

@RedisHash(value="TaxisOnWait", timeToLive=300L)
@Getter @Setter
public class TaxiOnWait {
	@Id
	private String taxiId;
	private int count;
}
