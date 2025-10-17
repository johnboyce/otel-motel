package com.johnnyb.service;

import io.quarkus.runtime.StartupEvent;

public interface IDataInitializationService {
    void onStart(StartupEvent ev);
}
