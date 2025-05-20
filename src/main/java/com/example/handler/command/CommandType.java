package com.example.handler.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CommandType {

    START("/start", "Beginning"),
    MENU("/menu","Main menu"),
    INSTRUCTION("/instruction", "Instruction"),
    WITHDRAW("/getstars", "Get stars"),
    REFERRAL("/referral", "Invite friends"),
    HELP("/help", "HELP"),
    TASKS("/tasks", "Available tasks")

    ;

    String name;
    String description;

}
