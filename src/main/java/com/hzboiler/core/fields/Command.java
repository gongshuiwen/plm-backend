package com.hzboiler.core.fields;

import com.hzboiler.core.entity.BaseEntity;
import lombok.Getter;

import java.util.List;

/**
 * @author gongshuiwen
 */
@Getter
public class Command<T extends BaseEntity> {

    private CommandType commandType;
    private List<Long> ids;
    private List<T> entities;

    // ===================================================
    // Static factory methods for Many2Many field commands
    // ===================================================
    public static <T extends BaseEntity> Command<T> add(List<Long> ids) {
        Command<T> command = new Command<>();
        command.commandType = CommandType.ADD;
        command.ids = ids;
        return command;
    }

    public static <T extends BaseEntity> Command<T> remove(List<Long> ids) {
        Command<T> command = new Command<>();
        command.commandType = CommandType.REMOVE;
        command.ids = ids;
        return command;
    }

    public static <T extends BaseEntity> Command<T> replace(List<Long> ids) {
        Command<T> command = new Command<>();
        command.commandType = CommandType.REPLACE;
        command.ids = ids;
        return command;
    }

    // ==================================================
    // Static factory methods for One2Many field commands
    // ==================================================
    public static <T extends BaseEntity> Command<T> create(List<T> entities) {
        Command<T> command = new Command<>();
        command.commandType = CommandType.CREATE;
        command.entities = entities;
        return command;
    }

    public static <T extends BaseEntity> Command<T> delete(List<Long> ids) {
        Command<T> command = new Command<>();
        command.commandType = CommandType.DELETE;
        command.ids = ids;
        return command;
    }

    public static <T extends BaseEntity> Command<T> update(Long id, T entity) {
        Command<T> command = new Command<>();
        command.commandType = CommandType.UPDATE;
        command.ids = List.of(id);
        command.entities = List.of(entity);
        return command;
    }
}
