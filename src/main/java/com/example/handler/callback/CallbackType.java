package com.example.handler.callback;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CallbackType {
    EARN("⭐ Earn Stars"),
    INSTRUCTION("\uD83D\uDCD8 Instruction"),
    WITHDRAW("\uD83C\uDF81 Get Stars"),
    REFERRAL("\uD83D\uDCE2 Referrals"),
    HELP("\uD83D\uDEE0\uFE0F Help"),
    CHECK("✅ Check action"),
    TASK1("Task 1"),
    SELECT_GIFT("Gift"),
    CONFIRM_WITHDRAW("Confirm"),
    GIFT_DONE("❌Not completed"),

    ;

    String buttonText;
}
