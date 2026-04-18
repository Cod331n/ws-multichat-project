package ru.geoderma.service.telegram.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class ClientConnectionMessage {

	private String platformId;

	private String chatId;

	private String botId;

	private Set<String> forbiddenPlatforms;

}
