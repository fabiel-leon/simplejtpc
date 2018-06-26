package com.fabiel.jtpc2.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/*
 * basE91 encoding/decoding routines
 *
 * Copyright (c) 2000-2006 Joachim Henke
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of Joachim Henke nor the names of his contributors may
 *    be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class basE91 {

    public static void encode(InputStream is, OutputStream os) {
        int s;
        byte[] ibuf = new byte[53248];
        byte[] obuf = new byte[65536];
        basE91 b91 = new basE91();
        try {
            while ((s = is.read(ibuf)) > 0) {
                s = b91.encode(ibuf, s, obuf);
                os.write(obuf, 0, s);
            }
            s = b91.encEnd(obuf);
            os.write(obuf, 0, s);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private int ebq, en, dbq, dn, dv;
    public final byte[] enctab;
    private final byte[] dectab;

    private int encode(byte[] ib, int n, byte[] ob) {
        int i, c = 0;

        for (i = 0; i < n; ++i) {
            ebq |= (ib[i] & 255) << en;
            en += 8;
            if (en > 13) {
                int ev = ebq & 8191;

                if (ev > 88) {
                    ebq >>= 13;
                    en -= 13;
                } else {
                    ev = ebq & 16383;
                    ebq >>= 14;
                    en -= 14;
                }
                ob[c++] = enctab[ev % 91];
                ob[c++] = enctab[ev / 91];
            }
        }
        return c;
    }

    private int encEnd(byte[] ob) {
        int c = 0;

        if (en > 0) {
            ob[c++] = enctab[ebq % 91];
            if (en > 7 || ebq > 90) {
                ob[c++] = enctab[ebq / 91];
            }
        }
        encReset();
        return c;
    }

    private void encReset() {
        ebq = 0;
        en = 0;
    }

    public int decode(byte[] ib, int n, byte[] ob) {
        int i, c = 0;

        for (i = 0; i < n; ++i) {
            if (dectab[ib[i]] == -1) {
                continue;
            }
            if (dv == -1) {
                dv = dectab[ib[i]];
            } else {
                dv += dectab[ib[i]] * 91;
                dbq |= dv << dn;
                dn += (dv & 8191) > 88 ? 13 : 14;
                do {
                    ob[c++] = (byte) dbq;
                    dbq >>= 8;
                    dn -= 8;
                } while (dn > 7);
                dv = -1;
            }
        }
        return c;
    }

    public int decEnd(byte[] ob) {
        int c = 0;

        if (dv != -1) {
            ob[c++] = (byte) (dbq | dv << dn);
        }
        decReset();
        return c;
    }

    private void decReset() {
        dbq = 0;
        dn = 0;
        dv = -1;
    }

    public static void encodeWrap(InputStream is, OutputStream os) {
        int i, s;
        int n = 0;
        byte[] ibuf = new byte[53248];
        byte[] obuf = new byte[65536];
        char[] line = new char[76];
        basE91 b91 = new basE91();

        try {
            PrintStream ps = new PrintStream(os, false, "US-ASCII");
            while ((s = is.read(ibuf)) > 0) {
                s = b91.encode(ibuf, s, obuf);
                for (i = 0; i < s; ++i) {
                    line[n++] = (char) obuf[i];
                    if (n == 76) {

                        ps.print(String.valueOf(line) + "\r\n");
                        n = 0;
                    }
                }
            }
            s = b91.encEnd(obuf);
            for (i = 0; i < s; ++i) {
                line[n++] = (char) obuf[i];
                if (n == 76) {
                    ps.print(String.valueOf(line) + "\r\n");
                    n = 0;
                }
            }
            if (n > 0) {
                ps.print(new String(line, 0, n) + "\r\n");
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void decode(InputStream is, OutputStream os) {
        int s;
        byte[] ibuf = new byte[65536];
        byte[] obuf = new byte[57344];
        basE91 b91 = new basE91();

        try {
            while ((s = is.read(ibuf)) > 0) {
                s = b91.decode(ibuf, s, obuf);
                os.write(obuf, 0, s);
            }
            s = b91.decEnd(obuf);
            os.write(obuf, 0, s);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private basE91() {
        int i;
        String ts = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,./:;<=>?@[]^_`{|}~\"";

        enctab = ts.getBytes();
        dectab = new byte[256];
        for (i = 0; i < 256; ++i) {
            dectab[i] = -1;
        }
        for (i = 0; i < 91; ++i) {
            dectab[enctab[i]] = (byte) i;
        }
        encReset();
        decReset();
    }
}
