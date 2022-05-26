package com.smoc.cloud.filters.configuration;

import com.smoc.cloud.filters.utils.DFA.FilterInitialize;
import com.smoc.cloud.filters.service.FiltersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FilterDataInitListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private FiltersService filtersService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        FilterInitialize.sensitiveWordsFilter.initializeSensitiveWords(filtersService.getSensitiveWords());
        FilterInitialize.checkWordsFilter.initializeCheckWords(filtersService.getCheckWords());
        FilterInitialize.superWhiteWordsFilter.initializeSuperWhiteWords(filtersService.getSuperWhiteWords());
        FilterInitialize.infoTypeSensitiveMap = filtersService.getInfoTypeSensitiveWords();
        FilterInitialize.accountSensitiveMap = filtersService.getAccountSensitiveWords();
        FilterInitialize.accountCheckMap = filtersService.getAccountCheckWords();
        FilterInitialize.accountSuperWhiteMap = filtersService.getAccountSuperWhiteWords();
    }
}

