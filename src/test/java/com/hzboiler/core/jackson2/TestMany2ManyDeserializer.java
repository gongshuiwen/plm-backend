package com.hzboiler.core.jackson2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hzboiler.core.fields.Command;
import com.hzboiler.core.fields.CommandType;
import com.hzboiler.core.fields.Many2Many;
import com.hzboiler.core.jackson2.Many2ManyDeserializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestMany2ManyDeserializer {

    ObjectMapper mapper;
    {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Many2Many.class, new Many2ManyDeserializer(Many2Many.class));
        mapper.registerModule(module);
    }

    @Test
    public void testCommandAdd() throws JsonProcessingException {
        Many2Many<?> many2Many = mapper.readValue("[[0, [1, 2, 3]]]", Many2Many.class);
        assertEquals(1, many2Many.getCommands().size());

        Command<?> command = many2Many.getCommands().get(0);
        assertEquals(CommandType.ADD, command.getCommandType());
        assertEquals(List.of(1L, 2L, 3L), command.getIds());
        assertNull(command.getEntities());
    }

    @Test
    public void testCommandRemove() throws JsonProcessingException {
        Many2Many<?> many2Many = mapper.readValue("[[2, [1, 2, 3]]]", Many2Many.class);
        assertEquals(1, many2Many.getCommands().size());

        Command<?> command = many2Many.getCommands().get(0);
        assertEquals(CommandType.REMOVE, command.getCommandType());
        assertEquals(List.of(1L, 2L, 3L), command.getIds());
        assertNull(command.getEntities());
    }

    @Test
    public void testCommandReplace() throws JsonProcessingException {
        Many2Many<?> many2Many = mapper.readValue("[[4, [1, 2, 3]]]", Many2Many.class);
        assertEquals(1, many2Many.getCommands().size());

        Command<?> command = many2Many.getCommands().get(0);
        assertEquals(CommandType.REPLACE, command.getCommandType());
        assertEquals(List.of(1L, 2L, 3L), command.getIds());
        assertNull(command.getEntities());
    }
}