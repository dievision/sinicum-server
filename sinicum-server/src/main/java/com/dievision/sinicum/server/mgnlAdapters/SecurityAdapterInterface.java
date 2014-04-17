package com.dievision.sinicum.server.mgnlAdapters;

import javax.security.auth.Subject;

public interface SecurityAdapterInterface {
    Subject getSystemSubject();
}
