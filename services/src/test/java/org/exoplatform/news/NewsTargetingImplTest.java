package org.exoplatform.news;

import org.exoplatform.news.rest.NewsTargetingEntity;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.*", "org.w3c.*", "javax.naming.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class NewsTargetingImplTest {

  @Mock
  MetadataService metadataService;

  @Test
  public void shouldReturnTargets() {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService);
    MetadataType metadataType = new MetadataType(4, "newsTarget");
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("label", "slider");
    HashMap<String, String> latestNewsProperties = new HashMap<>();
    latestNewsProperties.put("label", "latest");
    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    sliderNews.setName("slider news");
    sliderNews.setCreatedDate(100);
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);
    newsTargets.add(sliderNews);

    Metadata newsTarget2 = new Metadata();
    newsTarget2.setName("latest news");
    newsTarget2.setCreatedDate(200);
    newsTarget2.setProperties(latestNewsProperties);
    newsTarget2.setId(2);
    newsTargets.add(newsTarget2);

    // When
    when(metadataService.getMetadatas(metadataType.getName(),100)).thenReturn(newsTargets);
    List<NewsTargetingEntity> newsTargetingEntities = newsTargetingService.getTargets();

    // Then
    assertNotNull(newsTargetingEntities);
    assertEquals(2, newsTargetingEntities.size());
    assertEquals("slider news", newsTargetingEntities.get(0).getName());
    assertEquals("latest news", newsTargetingEntities.get(1).getName());
    assertEquals("slider", newsTargetingEntities.get(0).getLabel());
    assertEquals("latest", newsTargetingEntities.get(1).getLabel());
  }

}
