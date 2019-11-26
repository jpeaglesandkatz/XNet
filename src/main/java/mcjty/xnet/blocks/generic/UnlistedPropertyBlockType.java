package mcjty.xnet.blocks.generic;

import mcjty.xnet.blocks.cables.ConnectorType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.state.IProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class UnlistedPropertyBlockType implements IProperty<ConnectorType> {

    private final String name;

    public UnlistedPropertyBlockType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<ConnectorType> getAllowedValues() {
        return Arrays.asList(ConnectorType.VALUES);
    }

    @Override
    public Class<ConnectorType> getValueClass() {
        return ConnectorType.class;
    }

    @Override
    public Optional<ConnectorType> parseValue(String value) {
        return Optional.of(ConnectorType.valueOf(value));
    }

    @Override
    public String getName(ConnectorType value) {
        return name;
    }
}
