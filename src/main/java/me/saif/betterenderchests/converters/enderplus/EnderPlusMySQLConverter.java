package me.saif.betterenderchests.converters.enderplus;

import me.saif.betterenderchests.VariableEnderChests;

public class EnderPlusMySQLConverter extends EnderPlusConverter {

    public EnderPlusMySQLConverter(VariableEnderChests plugin) {
        super(plugin, "EnderPlusMySQL");
    }

    @Override
    public boolean convert() {
        return false;
    }
}
