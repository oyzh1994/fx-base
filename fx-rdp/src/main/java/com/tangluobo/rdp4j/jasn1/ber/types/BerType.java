package com.tangluobo.rdp4j.jasn1.ber.types;

import com.tangluobo.rdp4j.jasn1.ber.BerByteArrayOutputStream;
import com.tangluobo.rdp4j.jasn1.ber.BerTag;

import java.io.IOException;
import java.io.InputStream;

public interface BerType {

    int encode(BerByteArrayOutputStream os) throws IOException;

    int encode(BerByteArrayOutputStream os, boolean withTag) throws IOException;
    
    int decode(InputStream is) throws IOException;

    int decode(InputStream is, boolean withTag) throws IOException;
    
    BerTag getTag();
}
