/*
 * Copyright (C) 2022 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and-or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 */
package org.exoplatform.news.listener;

import org.exoplatform.news.model.News;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
@Asynchronous
public class TestNewNewsListener extends Listener<String, News> {

  private static final Log LOG =
      ExoLogger.getLogger(TestNewNewsListener.class);

  @Override
  public void onEvent(Event<String, News> event) throws Exception {
    LOG.info("Start event");
  }

}
