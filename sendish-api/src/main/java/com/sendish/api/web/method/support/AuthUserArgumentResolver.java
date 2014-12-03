package com.sendish.api.web.method.support;

/*******************************************************************************
 * Copyright (C) 2011-2014 Tri plus grupa d.o.o <info@3plus.hr>
 * All rights reserved.
 * 
 * This file is part of project zipato-admin.
 * It can not be copied and/or distributed without the
 * express permission of Tri plus grupa d.o.o
 ******************************************************************************/

import com.sendish.api.security.userdetails.AuthUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(AuthUser.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest,
                                  final WebDataBinderFactory binderFactory) throws Exception {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUser) {
            return principal;
        }

        return null;
    }

}
