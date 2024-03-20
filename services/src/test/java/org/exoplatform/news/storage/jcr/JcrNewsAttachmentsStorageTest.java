package org.exoplatform.news.storage.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.news.storage.NewsAttachmentsStorage;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

@RunWith(MockitoJUnitRunner.class)
public class JcrNewsAttachmentsStorageTest {

  @Mock
  RepositoryService       repositoryService;

  @Mock
  SessionProviderService  sessionProviderService;

  @Mock
  ManageableRepository    repository;

  @Mock
  RepositoryEntry         repositoryEntry;

  @Mock
  SessionProvider         sessionProvider;

  @Mock
  Session                 session;

  @Test
  public void shouldGetAttachmentsOfANewsWhenThereAre() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage();
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues())
             .thenReturn(new Value[] { new StringValue("idAttach1"), new StringValue("idAttach2"),
                 new StringValue("idAttach3") });
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Node attachmentNode = mock(Node.class);
    lenient().when(attachmentNode.getPath()).thenReturn("/folder/subFolder/attachNode");
    Node attachmentNode1 = mock(Node.class);
    lenient().when(attachmentNode1.getPath()).thenReturn("/folder/subFolder/attachNode1");
    Node attachmentNode2 = mock(Node.class);
    // quarantined attachment
    lenient().when(attachmentNode2.getPath()).thenReturn("/Quarantine/attachNode2");
    lenient().when(session.getNodeByUUID(eq("idAttach1"))).thenReturn(attachmentNode);
    lenient().when(session.getNodeByUUID(eq("idAttach2"))).thenReturn(attachmentNode1);
    lenient().when(session.getNodeByUUID(eq("idAttach3"))).thenReturn(attachmentNode2);

    // When
    List<NewsAttachment> newsAttachments = newsAttachmentsService.getNewsAttachments(node);

    // Then
    assertNotNull(newsAttachments);
    assertEquals(2, newsAttachments.size());
  }

  @Test
  public void shouldGetNoAttachmentsOfANewsWhenThereAreNot() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage();
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues()).thenReturn(new Value[] {});
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);

    // When
    List<NewsAttachment> newsAttachments = newsAttachmentsService.getNewsAttachments(node);

    // Then
    assertNotNull(newsAttachments);
    assertEquals(0, newsAttachments.size());
  }

  @Test
  public void shouldMakeAttachmentsPublic() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage();
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues())
             .thenReturn(new Value[] { new StringValue("idAttach1"), new StringValue("idAttach2"),
                 new StringValue("idAttach3") });
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    ExtendedNode attachmentNode1 = mock(ExtendedNode.class);
    lenient().when(attachmentNode1.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    lenient().when(attachmentNode1.getPath()).thenReturn("/folder/subFolder/attachNode1");
    ExtendedNode attachmentNode2 = mock(ExtendedNode.class);
    lenient().when(attachmentNode2.canAddMixin(eq("exo:privilegeable"))).thenReturn(false);
    lenient().when(attachmentNode2.getPath()).thenReturn("/folder/subFolder/attachNode2");
    ExtendedNode attachmentNode3 = mock(ExtendedNode.class);
    lenient().when(attachmentNode3.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    lenient().when(attachmentNode3.getPath()).thenReturn("/folder/subFolder/attachNode3");
    lenient().when(session.getNodeByUUID(eq("idAttach1"))).thenReturn(attachmentNode1);
    lenient().when(session.getNodeByUUID(eq("idAttach2"))).thenReturn(attachmentNode2);
    lenient().when(session.getNodeByUUID(eq("idAttach3"))).thenReturn(attachmentNode3);

    // When
    newsAttachmentsService.makeAttachmentsPublic(node);

    // Then
    verify(attachmentNode1, times(1)).setPermission(eq("any"), AdditionalMatchers.aryEq(new String[] { PermissionType.READ }));
    verify(attachmentNode1, times(1)).addMixin(eq("exo:privilegeable"));
    verify(attachmentNode2, times(1)).setPermission(eq("any"), AdditionalMatchers.aryEq(new String[] { PermissionType.READ }));
    verify(attachmentNode2, times(0)).addMixin(eq("exo:privilegeable"));
    verify(attachmentNode3, times(1)).setPermission(eq("any"), AdditionalMatchers.aryEq(new String[] { PermissionType.READ }));
    verify(attachmentNode3, times(1)).addMixin(eq("exo:privilegeable"));
  }

  @Test
  public void shouldUnmakeAttachmentsPublic() throws Exception {
    // Given
    NewsAttachmentsStorage newsAttachmentsService = new JcrNewsAttachmentsStorage();
    lenient().when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    lenient().when(repositoryService.getCurrentRepository()).thenReturn(repository);
    lenient().when(repository.getConfiguration()).thenReturn(repositoryEntry);
    lenient().when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    lenient().when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    lenient().when(node.getUUID()).thenReturn("id123");
    lenient().when(node.getName()).thenReturn("name123");
    lenient().when(node.getSession()).thenReturn(session);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Property attachmentIdsProperty = mock(Property.class);
    lenient().when(attachmentIdsProperty.getValues())
             .thenReturn(new Value[] { new StringValue("idAttach1"), new StringValue("idAttach2"),
                 new StringValue("idAttach3") });
    lenient().when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    lenient().when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    lenient().when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    ExtendedNode attachmentNode1 = mock(ExtendedNode.class);
    lenient().when(attachmentNode1.isNodeType(eq("exo:privilegeable"))).thenReturn(true);
    lenient().when(attachmentNode1.getPath()).thenReturn("/folder/subFolder/attachNode1");
    ExtendedNode attachmentNode2 = mock(ExtendedNode.class);
    lenient().when(attachmentNode2.isNodeType(eq("exo:privilegeable"))).thenReturn(false);
    lenient().when(attachmentNode2.getPath()).thenReturn("/folder/subFolder/attachNode2");
    ExtendedNode attachmentNode3 = mock(ExtendedNode.class);
    lenient().when(attachmentNode3.isNodeType(eq("exo:privilegeable"))).thenReturn(true);
    lenient().when(attachmentNode3.getPath()).thenReturn("/folder/subFolder/attachNode3");
    lenient().when(session.getNodeByUUID(eq("idAttach1"))).thenReturn(attachmentNode1);
    lenient().when(session.getNodeByUUID(eq("idAttach2"))).thenReturn(attachmentNode2);
    lenient().when(session.getNodeByUUID(eq("idAttach3"))).thenReturn(attachmentNode3);

    // When
    newsAttachmentsService.unmakeAttachmentsPublic(node);

    // Then
    verify(attachmentNode1, times(1)).removePermission(eq("any"));
    verify(attachmentNode2, times(0)).removePermission(eq("any"));
    verify(attachmentNode3, times(1)).removePermission(eq("any"));
  }
}
