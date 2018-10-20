package org.lockss.crawljax;


/*
 * $Id: DefLockssConfigurationBuilder.java,v 1.1 2014/04/14 23:08:24 clairegriffin Exp $
 */

/*

Copyright (c) 2000-2014 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Stanford University shall not
be used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from Stanford University.

*/

import com.crawljax.core.configuration.CrawlElement;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.plugin.Plugin;
import com.google.common.collect.ImmutableList;
import java.io.File;
import junit.framework.TestCase;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Test class for DefLockssConfigurationBuilder Created by claire on 3/18/14.
 */
public class TestDefLockssConfigurationBuilder extends TestCase {

  File mCrawljaxDir;
  File mCacheDir;
  DefLockssConfigurationBuilder mConfigBuilder;
  String m_testUrl = "http://www.example.com";
  String mConfigFileName;
  String mCacheDirName;
  private PropertiesConfiguration mDefaultConfig =
      DefLockssConfigurationBuilder.defaultConfig();

  @Before
  public void setUp() throws Exception {
    super.setUp();
    mCrawljaxDir = new File(FileUtils.getTempDirectory(), "crawljax");
    mCacheDir = new File(mCrawljaxDir, "cache");
    mCacheDir.mkdirs();
    mCacheDirName = mCacheDir.getAbsolutePath();
    mConfigBuilder = new DefLockssConfigurationBuilder();
    File config = new File(mCrawljaxDir, "lockss.config");
    mConfigFileName = config.getAbsolutePath();
  }


  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(mCrawljaxDir);
    super.tearDown();
  }

  public void testConfigure() throws Exception {
    CrawljaxConfigurationBuilder ccbuilder;
    CrawljaxConfiguration crawljaxConfig;


    // test null configfile - valid input
    ccbuilder = mConfigBuilder.configure(m_testUrl, mCacheDirName, null);
    crawljaxConfig = ccbuilder.build();

    assertEquals(DefLockssConfigurationBuilder.MAX_STATES_DEFAULT,
        crawljaxConfig.getMaximumStates());
    assertEquals(DefLockssConfigurationBuilder.DEPTH_DEFAULT,
        crawljaxConfig.getMaximumDepth());
    assertEquals(DefLockssConfigurationBuilder.TIMEOUT_DEFAULT * 60000,
        crawljaxConfig.getMaximumRuntime());
    assertEquals(DefLockssConfigurationBuilder.BROWSER_DEFAULT,
        crawljaxConfig.getBrowserConfig().getBrowsertype());

    // test null input.
    try {
      ccbuilder = mConfigBuilder.configure(null, mCacheDirName,
          mConfigFileName);
      crawljaxConfig = ccbuilder.build();
      fail("null url should throw null pointer exception");
    }
    catch (NullPointerException npe) {

    }

    // test null cache dir
    try {
      ccbuilder = mConfigBuilder.configure(m_testUrl, null, mConfigFileName);
      crawljaxConfig = ccbuilder.build();
      fail("null output should throw null pointer exception");
    }
    catch (NullPointerException npe) {

    }
    // test configure with all data
    int maxStates = 100;
    int depth = 1;
    long runtime = 10;
    String browser = "chrome";

    createMockConfig(mConfigFileName, maxStates, depth, runtime, browser,
        DefLockssConfigurationBuilder.PROXY_PARAM_DEFAULT);

    assertTrue(new File(mConfigFileName).exists());
    ccbuilder = mConfigBuilder.configure(m_testUrl, mCacheDirName, mConfigFileName);
    crawljaxConfig = ccbuilder.build();

    assertEquals(maxStates, crawljaxConfig.getMaximumStates());
    assertEquals(depth, crawljaxConfig.getMaximumDepth());
    assertEquals(runtime * 60000, crawljaxConfig.getMaximumRuntime());
    assertEquals(browser.toUpperCase(),
        crawljaxConfig.getBrowserConfig().getBrowsertype().name());

  }

  public void testConfigureCrawlRules() throws Exception {
    CrawljaxConfigurationBuilder ccbuilder =
        CrawljaxConfiguration.builderFor(m_testUrl);
    mConfigBuilder.setConfig(mDefaultConfig);
    mConfigBuilder.configureCrawlRules(ccbuilder);
    CrawljaxConfiguration cconfig = ccbuilder.build();
    CrawlRules rules = cconfig.getCrawlRules();
    assertEquals(DefLockssConfigurationBuilder.CRAWL_HIDDEN_DEFAULT,
        rules.isCrawlHiddenAnchors());
    assertEquals(DefLockssConfigurationBuilder.CLICK_ONCE_DEFAULT,
        rules.isClickOnce());
    assertEquals(DefLockssConfigurationBuilder.CRAWL_FRAMES_DEFAULT,
        rules.shouldCrawlFrames());
    assertEquals(DefLockssConfigurationBuilder.INSERT_RANDOM_DATA_DEFAULT,
        rules.isRandomInputInForms());
    assertEquals(DefLockssConfigurationBuilder.TIMEOUT_DEFAULT * 60000,
        cconfig.getMaximumRuntime());
    assertEquals(DefLockssConfigurationBuilder.WAIT_AFTER_EVENT_DEFAULT,
        rules.getWaitAfterEvent());
    assertEquals(DefLockssConfigurationBuilder.WAIT_AFTER_RELOAD_DEFAULT,
        rules.getWaitAfterReloadUrl());
    // reassign the defaults
    PropertiesConfiguration config = new PropertiesConfiguration();

    config.setProperty(DefLockssConfigurationBuilder.CRAWL_HIDDEN_PARAM,
        !DefLockssConfigurationBuilder.CRAWL_HIDDEN_DEFAULT);
    config.setProperty(DefLockssConfigurationBuilder.CLICK_ONCE_PARAM,
        !DefLockssConfigurationBuilder.CLICK_ONCE_DEFAULT);
    config.setProperty(DefLockssConfigurationBuilder.TIMEOUT_PARAM,
        2 * DefLockssConfigurationBuilder.TIMEOUT_DEFAULT);
    config.setProperty(DefLockssConfigurationBuilder.WAIT_AFTER_RELOAD_PARAM,
        2 * DefLockssConfigurationBuilder.WAIT_AFTER_RELOAD_DEFAULT);
    config.setProperty(DefLockssConfigurationBuilder.WAIT_AFTER_EVENT_PARAM,
        2 * DefLockssConfigurationBuilder.WAIT_AFTER_EVENT_DEFAULT);
    config.setProperty(DefLockssConfigurationBuilder.CRAWL_FRAMES_PARAM,
        !DefLockssConfigurationBuilder.CRAWL_FRAMES_DEFAULT);
    config.setProperty(DefLockssConfigurationBuilder.INSERT_RANDOM_DATA_PARAM,
        !DefLockssConfigurationBuilder.INSERT_RANDOM_DATA_DEFAULT);
    mConfigBuilder.setConfig(config);
    mConfigBuilder.configureCrawlRules(ccbuilder);
    cconfig = ccbuilder.build();
    rules = cconfig.getCrawlRules();
    assertEquals(!DefLockssConfigurationBuilder.CRAWL_HIDDEN_DEFAULT,
        rules.isCrawlHiddenAnchors());
    assertEquals(!DefLockssConfigurationBuilder.CLICK_ONCE_DEFAULT,
        rules.isClickOnce());
    assertEquals(!DefLockssConfigurationBuilder.CRAWL_FRAMES_DEFAULT,
        rules.shouldCrawlFrames());
    assertEquals(!DefLockssConfigurationBuilder.INSERT_RANDOM_DATA_DEFAULT,
        rules.isRandomInputInForms());
    assertEquals(DefLockssConfigurationBuilder.TIMEOUT_DEFAULT * 60000 * 2,
        cconfig.getMaximumRuntime());
    assertEquals(DefLockssConfigurationBuilder.WAIT_AFTER_EVENT_DEFAULT * 2,
        rules.getWaitAfterEvent());
    assertEquals(DefLockssConfigurationBuilder.WAIT_AFTER_RELOAD_DEFAULT * 2,
        rules.getWaitAfterReloadUrl());
  }

  public void testConfigureCrawlClicks() throws Exception {
    CrawljaxConfigurationBuilder builder =
        CrawljaxConfiguration.builderFor(m_testUrl);
    mConfigBuilder.setConfig(mDefaultConfig);
    mConfigBuilder.configureCrawlClicks(builder);
    CrawljaxConfiguration cconfig = builder.build();
    CrawlRules rules = cconfig.getCrawlRules();
    ImmutableList<CrawlElement> included =
        rules.getPreCrawlConfig().getIncludedElements();
    assertEquals(1, included.size());
    ImmutableList<CrawlElement> excluded =
        rules.getPreCrawlConfig().getExcludedElements();
    assertEquals(0, excluded.size());

    // modify the rules by changing the basic config
    builder = CrawljaxConfiguration.builderFor(m_testUrl);
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty(DefLockssConfigurationBuilder.CLICK_PARAM,
        "A");
    config.setProperty(DefLockssConfigurationBuilder.DONT_CLICK_PARAM,
        "OPTION");
    config.setProperty(DefLockssConfigurationBuilder
            .DONT_CLICK_CHILDREN_PARAM,
        "FORM");
    mConfigBuilder.setConfig(config);
    mConfigBuilder.configureCrawlClicks(builder);
    cconfig = builder.build();
    rules = cconfig.getCrawlRules();
    included = rules.getPreCrawlConfig().getIncludedElements();
    assertEquals(1, included.size());
    excluded = rules.getPreCrawlConfig().getExcludedElements();
    assertEquals(2, excluded.size());

  }

  public void testConfigurePlugins() throws Exception {
    CrawljaxConfigurationBuilder builder =
        CrawljaxConfiguration.builderFor(m_testUrl);
    mConfigBuilder.setConfig(mDefaultConfig);
    // default config does not have any plugins
    assertFalse(mConfigBuilder.configurePlugins(builder));

    // add a plugin
  }


  public void testInstallLapProxyPlugin() throws Exception {
    CrawljaxConfigurationBuilder builder =
        CrawljaxConfiguration.builderFor(m_testUrl);
    mConfigBuilder.setOutDir(mCacheDirName);
    mConfigBuilder.setConfig(mDefaultConfig);
    mConfigBuilder.installWARCProxyPlugin(builder);
    CrawljaxConfiguration cconfig = builder.build();
    // test the scarab proxy has been installed
    ProxyConfiguration proxyConfig =
        cconfig.getProxyConfiguration();
    assertEquals(DefLockssConfigurationBuilder.WARC_PROXY_HOST_DEFAULT,
        proxyConfig.getHostname());
    assertEquals(DefLockssConfigurationBuilder.WARC_PROXY_WEB_PORT_DEFAULT,
        proxyConfig.getPort());
    // test the LapWarcOutput plugin as been installed
    ImmutableList<Plugin> plugins = cconfig.getPlugins();
    //assertEquals(1, plugins.size());
    //assertTrue(plugins.get(0) instanceof LapWarcOutput);
  }

  public void testAvailableBrowsers() throws Exception {
    String expected = "FIREFOX,INTERNET_EXPLORER,CHROME,REMOTE,PHANTOMJS";
    String actual = mConfigBuilder.availableBrowsers();
    assertEquals(expected, actual);
  }

  private void createMockConfig(String fileName, int maxStates, int depth,
      long runtime, String browser, String proxy)  throws ConfigurationException{
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty(DefLockssConfigurationBuilder.MAX_STATES_PARAM, maxStates);
    config.setProperty(DefLockssConfigurationBuilder.DEPTH_PARAM, depth);
    config.setProperty(DefLockssConfigurationBuilder.TIMEOUT_PARAM, runtime);
    config.setProperty(DefLockssConfigurationBuilder.BROWSER_PARAM, browser);
    config.setProperty(DefLockssConfigurationBuilder.PROXY_PARAM, proxy);
    config.save(fileName);
 }

}
