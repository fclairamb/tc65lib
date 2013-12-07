package org.javacint.apnauto;

import java.io.IOException;

public interface GPRSParametersProvider {
    public GPRSSettings next() throws IOException;
}
