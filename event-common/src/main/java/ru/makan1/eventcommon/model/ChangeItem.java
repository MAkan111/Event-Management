package ru.makan1.eventcommon.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChangeItem {
    private String field;
    private Object oldValue;
    private Object newValue;
}
