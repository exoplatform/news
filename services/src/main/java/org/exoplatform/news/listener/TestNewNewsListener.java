/*
 * Copyright (C) 2022 eXo Platform SAS.
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
