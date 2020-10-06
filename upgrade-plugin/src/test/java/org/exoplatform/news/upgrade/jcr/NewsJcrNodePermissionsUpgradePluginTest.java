package org.exoplatform.news.upgrade.jcr;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

@RunWith(PowerMockRunner.class)
public class NewsJcrNodePermissionsUpgradePluginTest {

  @Mock
  NodeHierarchyCreator   nodeHierarchyCreator;
  
  @Mock
  RepositoryService      repositoryService;
  
  @Mock
  SessionProviderService sessionProviderService;

  @Mock
  ManageableRepository   repository;

  @Mock
  RepositoryEntry        repositoryEntry;

  @Mock
  SessionProvider        sessionProvider;
  
  @Mock
  Session                session;

  @Mock
  NodeIterator           nodeIterator;

  @Test
  public void testNewsJcrNodeMigration() throws Exception {
    InitParams initParams = new InitParams();

    ValueParam valueParam = new ValueParam();
    valueParam.setName("product.group.id");
    valueParam.setValue("org.exoplatform.addons.news");
    initParams.addParameter(valueParam);

    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(nodeHierarchyCreator.getJcrPath(anyString())).thenReturn(BasePath.CMS_GROUPS_PATH);
    Node spacesRootNode = mock(Node.class);
    when((Node)session.getItem(anyString())).thenReturn(spacesRootNode);
    when(spacesRootNode.getNodes()).thenReturn(nodeIterator);
    when(nodeIterator.hasNext()).thenReturn(true, true, false);
    Node spaceNode = mock(Node.class);
    when(nodeIterator.nextNode()).thenReturn(spaceNode);
    when(spaceNode.hasNode(NewsJcrNodePermissionsUpgradePlugin.NEWS_NODES_FOLDER)).thenReturn(true);
    ExtendedNode spaceNewsExtendedRootNode = mock(ExtendedNode.class);
    when(spaceNode.getNode(NewsJcrNodePermissionsUpgradePlugin.NEWS_NODES_FOLDER)).thenReturn(spaceNewsExtendedRootNode);
    spaceNewsExtendedRootNode.setPermission(NewsJcrNodePermissionsUpgradePlugin.ADMINISTRATORS_IDENTITY, PermissionType.ALL);
    NewsJcrNodePermissionsUpgradePlugin newsJcrNodePermissionsUpgradePlugin = new NewsJcrNodePermissionsUpgradePlugin(initParams,
                                                                                                                      nodeHierarchyCreator,
                                                                                                                      repositoryService,
                                                                                                                      sessionProviderService);
    newsJcrNodePermissionsUpgradePlugin.processUpgrade(null, null);
    verify(spaceNewsExtendedRootNode, times(2)).save();
    assertEquals(2, newsJcrNodePermissionsUpgradePlugin.getNewsJcrNodesUpdatedCount());
  }
}
