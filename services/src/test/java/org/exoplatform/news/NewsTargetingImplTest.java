package org.exoplatform.news;

import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsTargetObject;
import org.exoplatform.news.rest.NewsTargetingEntity;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.service.impl.NewsTargetingServiceImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.*", "org.w3c.*", "javax.naming.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class NewsTargetingImplTest {

  @Mock
  MetadataService metadataService;
  
  @Mock
  IdentityManager identityManager;

  @Test
  public void shouldReturnTargets() {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    MetadataType metadataType = new MetadataType(4, "newsTarget");
    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("label", "slider news");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);
    newsTargets.add(sliderNews);

    Metadata newsTarget2 = new Metadata();
    newsTarget2.setName("latestNews");
    newsTarget2.setCreatedDate(200);
    HashMap<String, String> latestNewsProperties = new HashMap<>();
    latestNewsProperties.put("label", "latest news");
    newsTarget2.setProperties(latestNewsProperties);
    newsTarget2.setId(2);
    newsTargets.add(newsTarget2);

    when(metadataService.getMetadatas(metadataType.getName(),100)).thenReturn(newsTargets);

    // When
    List<NewsTargetingEntity> newsTargetingEntities = newsTargetingService.getTargets();

    // Then
    assertNotNull(newsTargetingEntities);
    assertEquals(2, newsTargetingEntities.size());
    assertEquals("sliderNews", newsTargetingEntities.get(0).getName());
    assertEquals("latestNews", newsTargetingEntities.get(1).getName());
    assertEquals("slider news", newsTargetingEntities.get(0).getLabel());
    assertEquals("latest news", newsTargetingEntities.get(1).getLabel());
  }

  @Test
  public void shouldNotReturnReferencedTargetsWhenReferencedIsFalse() {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "false");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);
    newsTargets.add(sliderNews);

    // When
    List<NewsTargetingEntity> newsTargetingEntities = newsTargetingService.getReferencedTargets();

    // Then
    assertNotNull(newsTargetingEntities);
    assertEquals(0, newsTargetingEntities.size());

  }

  @Test
  public void shouldReturnReferencedTargets() {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    List<Metadata> newsTargets = new LinkedList<>();
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "true");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);
    newsTargets.add(sliderNews);

    when(metadataService.getMetadatasByProperty("referenced","true", 100)).thenReturn(newsTargets);

    // When
    List<NewsTargetingEntity> newsTargetingEntities = newsTargetingService.getReferencedTargets();

    // Then
    assertNotNull(newsTargetingEntities);
    assertEquals(1, newsTargetingEntities.size());
    assertEquals("sliderNews", newsTargetingEntities.get(0).getName());
  }

  @Test
  public void shouldReturnNewsTargetsByNewsId() {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "true");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);

    List<MetadataItem> metadataItems = new LinkedList<>();
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setCreatedDate(100);
    metadataItem.setCreatorId(1);
    metadataItem.setId(1);
    metadataItem.setObjectId("123456");
    metadataItem.setMetadata(sliderNews);
    metadataItems.add(metadataItem);

    NewsTargetObject newsTargetObject = new NewsTargetObject("news", "123456", null);
    when(metadataService.getMetadataItemsByMetadataTypeAndObject(NewsTargetingService.METADATA_TYPE.getName(), newsTargetObject)).thenReturn(metadataItems);

    // When
    List<String> newsTargets = newsTargetingService.getTargetsByNewsId("123456");

    // Then
    assertNotNull(newsTargets);
    assertEquals(1, newsTargets.size());
    assertEquals("sliderNews", newsTargets.get(0));
  }

  @Test
  public void testSaveNewsTargets() throws Exception {
    // Given
    NewsTargetingServiceImpl newsTargetingService = new NewsTargetingServiceImpl(metadataService, identityManager);
    Metadata sliderNews = new Metadata();
    sliderNews.setName("sliderNews");
    sliderNews.setCreatedDate(100);
    HashMap<String, String> sliderNewsProperties = new HashMap<>();
    sliderNewsProperties.put("referenced", "true");
    sliderNews.setProperties(sliderNewsProperties);
    sliderNews.setId(1);

    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setCreatedDate(100);
    metadataItem.setCreatorId(1);
    metadataItem.setId(1);
    metadataItem.setObjectId("123456");
    metadataItem.setMetadata(sliderNews);

    List<String> targets = new LinkedList<>();
    targets.add("sliderNews");

    News news = new News();
    news.setSpaceId("spaceId");
    news.setTitle("Test news");
    news.setAuthor("user1");
    news.setTargets(targets);
    news.setId("123456");

    NewsTargetObject newsTargetObject = new NewsTargetObject("news", "123456", null);
    MetadataKey metadataKey = new MetadataKey(NewsTargetingService.METADATA_TYPE.getName(), "sliderNews", 0);
    Identity userIdentity = new Identity("1");
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "1")).thenReturn(userIdentity);
    when(metadataService.createMetadataItem(newsTargetObject, metadataKey, 1)).thenReturn(metadataItem);

    // When
    newsTargetingService.saveNewsTarget(news.getId(), news.getTargets(), "1");

    // Then
    verify(identityManager, times(1)).getOrCreateIdentity(OrganizationIdentityProvider.NAME, "1");
    verify(metadataService, times(1)).createMetadataItem(newsTargetObject, metadataKey, 1);
  }

}
