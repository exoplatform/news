package org.exoplatform.news.rest;

import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class NewsTargetingRestResourcesV1Test {

  @Mock
  NewsTargetingService newsTargetingService;

  @Before
  public void setup() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Test
  public void shouldReturnOkWhenGetTargets() {
    // Given
    NewsTargetingRestResourcesV1 newsTargetingRestResourcesV1 = new NewsTargetingRestResourcesV1(newsTargetingService);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    // When
    Response response = newsTargetingRestResourcesV1.getTargets(request);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnOkWhenGetReferencedTargets() {
    // Given
    NewsTargetingRestResourcesV1 newsTargetingRestResourcesV1 = new NewsTargetingRestResourcesV1(newsTargetingService);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    // When
    Response response = newsTargetingRestResourcesV1.getReferencedTargets(request);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

}
