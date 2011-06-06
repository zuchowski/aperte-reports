# The Grinder 3.4
# HTTP script recorded by TCPProxy at 2011-01-14 13:01:55

from net.grinder.script import Test
from net.grinder.script.Grinder import grinder
from net.grinder.plugin.http import HTTPPluginControl, HTTPRequest
from HTTPClient import NVPair
connectionDefaults = HTTPPluginControl.getConnectionDefaults()
httpUtilities = HTTPPluginControl.getHTTPUtilities()

# To use a proxy server, uncomment the next line and set the host and port.
# connectionDefaults.setProxyServer("localhost", 8001)

# These definitions at the top level of the file are evaluated once,
# when the worker process is started.

connectionDefaults.defaultHeaders = \
  [ NVPair('Accept-Language', 'en-us,en;q=0.5'),
    NVPair('Accept-Charset', 'ISO-8859-1,utf-8;q=0.7,*;q=0.7'),
    NVPair('Accept-Encoding', 'gzip, deflate'),
    NVPair('User-Agent', 'Mozilla/5.0 (Windows NT 5.1; rv:2.0b8) Gecko/20100101 Firefox/4.0b8'), ]

headers0= \
  [ NVPair('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'), ]

headers1= \
  [ NVPair('Accept', '*/*'),
    NVPair('Referer', 'http://saffron:18080/web/test/home'), ]

headers2= \
  [ NVPair('Accept', 'text/css,*/*;q=0.1'),
    NVPair('Referer', 'http://saffron:18080/web/test/home'), ]

headers3= \
  [ NVPair('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'),
    NVPair('Referer', 'http://saffron:18080/html/VAADIN/widgetsets/pl.net.bluesoft.rnd.vries.widgetset.VriesWidgetSet/33FB50D25766B6F52C88F45EBB85D5FB.cache.html'),
    NVPair('Cache-Control', 'no-cache'), ]

headers4= \
  [ NVPair('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'),
    NVPair('Referer', 'http://saffron:18080/html/VAADIN/widgetsets/pl.net.bluesoft.rnd.vries.widgetset.VriesWidgetSet/33FB50D25766B6F52C88F45EBB85D5FB.cache.html'), ]

url0 = 'http://saffron:18080'

# Create an HTTPRequest for each request, then replace the
# reference to the HTTPRequest with an instrumented version.
# You can access the unadorned instance using request101.__target__.
request101 = HTTPRequest(url=url0, headers=headers0)
request101 = Test(101, 'GET home').wrap(request101)

# request102 = HTTPRequest(url=url0, headers=headers1)
# request102 = Test(102, 'GET pl.net.bluesoft.rnd.vries.widgetset.VriesWidgetSet.nocache.js').wrap(request102)

# request103 = HTTPRequest(url=url0, headers=headers2)
# request103 = Test(103, 'GET styles.css').wrap(request103)

request201 = HTTPRequest(url=url0, headers=headers3)
request201 = Test(201, 'POST home').wrap(request201)

request301 = HTTPRequest(url=url0, headers=headers3)
request301 = Test(301, 'POST home').wrap(request301)

request401 = HTTPRequest(url=url0, headers=headers3)
request401 = Test(401, 'POST home').wrap(request401)

request501 = HTTPRequest(url=url0, headers=headers3)
request501 = Test(501, 'POST home').wrap(request501)

request601 = HTTPRequest(url=url0, headers=headers3)
request601 = Test(601, 'POST home').wrap(request601)

request701 = HTTPRequest(url=url0, headers=headers3)
request701 = Test(701, 'POST home').wrap(request701)

request801 = HTTPRequest(url=url0, headers=headers3)
request801 = Test(801, 'POST home').wrap(request801)

request901 = HTTPRequest(url=url0, headers=headers3)
request901 = Test(901, 'POST home').wrap(request901)

request1001 = HTTPRequest(url=url0, headers=headers4)
request1001 = Test(1001, 'GET home').wrap(request1001)


class TestRunner:
  """A TestRunner instance is created for each worker thread."""

  # A method for each recorded page.
  def page1(self):
    """GET home (requests 101-103)."""
    result = request101.GET('/web/test/home')
    self.token_browserId = \
      httpUtilities.valueFromBodyURI('browserId') # 'firefox'
    self.token_themeId = \
      httpUtilities.valueFromBodyURI('themeId') # 'classic'
    self.token_colorSchemeId = \
      httpUtilities.valueFromBodyURI('colorSchemeId') # '01'
    self.token_minifierType = \
      httpUtilities.valueFromBodyURI('minifierType') # 'css'
    self.token_languageId = \
      httpUtilities.valueFromBodyURI('languageId') # 'en_US'
    self.token_t = \
      httpUtilities.valueFromBodyURI('t') # '1288788527000'
    self.token_jsessionid = \
      httpUtilities.valueFromBodyURI('jsessionid') # '46b01991a0d8c32c8d2500c9cd0c'
    self.token_p_auth = \
      httpUtilities.valueFromBodyURI('p_auth') # 'B9WaJPDc'
    self.token_p_p_auth = \
      httpUtilities.valueFromBodyURI('p_p_auth') # 'nkEED4fu'
    self.token_p_p_id = \
      httpUtilities.valueFromBodyURI('p_p_id') # '49'
    self.token_p_p_lifecycle = \
      httpUtilities.valueFromBodyURI('p_p_lifecycle') # '1'
    self.token_p_p_state = \
      httpUtilities.valueFromBodyURI('p_p_state') # 'normal'
    self.token_p_p_mode = \
      httpUtilities.valueFromBodyURI('p_p_mode') # 'view'
    self.token_p_p_col_count = \
      httpUtilities.valueFromBodyURI('p_p_col_count') # '1'
    self.token__49_struts_action = \
      httpUtilities.valueFromBodyURI('_49_struts_action') # '/my_places/view'
    self.token__49_groupId = \
      httpUtilities.valueFromBodyURI('_49_groupId') # '10440'
    self.token__49_privateLayout = \
      httpUtilities.valueFromBodyURI('_49_privateLayout') # 'false'
    self.token_p_l_id = \
      httpUtilities.valueFromBodyURI('p_l_id') # '10563'

    # grinder.sleep(219)
    # request102.GET('/html/VAADIN/widgetsets/pl.net.bluesoft.rnd.vries.widgetset.VriesWidgetSet/pl.net.bluesoft.rnd.vries.widgetset.VriesWidgetSet.nocache.js' +
      # '?1295006761461')

    # grinder.sleep(157)
    # request103.GET('/html/VAADIN/themes/vries/styles.css')

    return result

  def page2(self):
    """POST home (request 201)."""
    self.token_p_p_id = \
      'Invokerportlet_WAR_ui001SNAPSHOT_INSTANCE_U7mb'
    self.token_p_p_lifecycle = \
      '2'
    self.token_p_p_resource_id = \
      'UIDL'
    self.token_p_p_cacheability = \
      'cacheLevelPage'
    self.token_p_p_col_id = \
      'column-2'
    self.token_repaintAll = \
      '1'
    self.token_sh = \
      '1050'
    self.token_sw = \
      '1400'
    self.token_cw = \
      '1383'
    self.token_ch = \
      '848'
    self.token_vw = \
      '838'
    self.token_vh = \
      '1'
    self.token_fr = \
      ''
    result = request201.POST('/web/test/home;jsessionid=' +
      self.token_jsessionid +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&p_p_col_id=' +
      self.token_p_p_col_id +
      '&p_p_col_count=' +
      self.token_p_p_col_count +
      '&repaintAll=' +
      self.token_repaintAll +
      '&sh=' +
      self.token_sh +
      '&sw=' +
      self.token_sw +
      '&cw=' +
      self.token_cw +
      '&ch=' +
      self.token_ch +
      '&vw=' +
      self.token_vw +
      '&vh=' +
      self.token_vh +
      '&fr=' +
      self.token_fr,
      'init',
      ( NVPair('Content-Type', 'text/plain;charset=utf-8'), ))

    return result

  def page3(self):
    """POST home (request 301)."""
    self.token_windowName = \
      'cb2b1033-c86b-41f0-a1e0-cc8d3dc6b5e5'
    result = request301.POST('/web/test/home;jsessionid=' +
      self.token_jsessionid +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&p_p_col_id=' +
      self.token_p_p_col_id +
      '&p_p_col_count=' +
      self.token_p_p_col_count +
      '&windowName=' +
      self.token_windowName,
      '7da10fbd-e641-4f03-b277-b9a90cb576fa848PID0heighti1383PID0widthiPID3filters-1PID3pagei',
      ( NVPair('Content-Type', 'text/plain;charset=utf-8'), ))

    return result

  def page4(self):
    """POST home (request 401)."""
    result = request401.POST('/web/test/home;jsessionid=' +
      self.token_jsessionid +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&p_p_col_id=' +
      self.token_p_p_col_id +
      '&p_p_col_count=' +
      self.token_p_p_col_count +
      '&windowName=' +
      self.token_windowName,
      '7da10fbd-e641-4f03-b277-b9a90cb576faPID3filters1PID3pagei',
      ( NVPair('Content-Type', 'text/plain;charset=utf-8'), ))

    return result

  def page5(self):
    """POST home (request 501)."""
    result = request501.POST('/web/test/home;jsessionid=' +
      self.token_jsessionid +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&p_p_col_id=' +
      self.token_p_p_col_id +
      '&p_p_col_count=' +
      self.token_p_p_col_count +
      '&windowName=' +
      self.token_windowName,
      '7da10fbd-e641-4f03-b277-b9a90cb576fa17PID3selectedctruePID4stateb',
      ( NVPair('Content-Type', 'text/plain;charset=utf-8'), ))

    return result

  def page6(self):
    """POST home (request 601)."""
    result = request601.POST('/web/test/home;jsessionid=' +
      self.token_jsessionid +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&p_p_col_id=' +
      self.token_p_p_col_id +
      '&p_p_col_count=' +
      self.token_p_p_col_count +
      '&windowName=' +
      self.token_windowName,
      '7da10fbd-e641-4f03-b277-b9a90cb576fa0PID5positionyi346PID5positionxi116PID5scrollTopi0PID5scrollLefti2011PID14yeari1PID14monthi2PID14dayi13PID14houri2PID14mini',
      ( NVPair('Content-Type', 'text/plain;charset=utf-8'), ))

    return result

  def page7(self):
    """POST home (request 701)."""
    result = request701.POST('/web/test/home;jsessionid=' +
      self.token_jsessionid +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&p_p_col_id=' +
      self.token_p_p_col_id +
      '&p_p_col_count=' +
      self.token_p_p_col_count +
      '&windowName=' +
      self.token_windowName,
      '7da10fbd-e641-4f03-b277-b9a90cb576fa2011PID15yeari1PID15monthi2PID15dayi13PID15houri2PID15mini',
      ( NVPair('Content-Type', 'text/plain;charset=utf-8'), ))

    return result

  def page8(self):
    """POST home (request 801)."""
    result = request801.POST('/web/test/home;jsessionid=' +
      self.token_jsessionid +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&p_p_col_id=' +
      self.token_p_p_col_id +
      '&p_p_col_count=' +
      self.token_p_p_col_count +
      '&windowName=' +
      self.token_windowName,
      '7da10fbd-e641-4f03-b277-b9a90cb576fa1PID22selectedc',
      ( NVPair('Content-Type', 'text/plain;charset=utf-8'), ))

    return result

  def page9(self):
    """POST home (request 901)."""
    result = request901.POST('/web/test/home;jsessionid=' +
      self.token_jsessionid +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&p_p_col_id=' +
      self.token_p_p_col_id +
      '&p_p_col_count=' +
      self.token_p_p_col_count +
      '&windowName=' +
      self.token_windowName,
      '7da10fbd-e641-4f03-b277-b9a90cb576fa86PID5scrollTopi0PID5scrollLeftitruePID25stateb',
      ( NVPair('Content-Type', 'text/plain;charset=utf-8'), ))

    return result

  def page10(self):
    """GET home (request 1001)."""
    self.token_p_p_resource_id = \
      'APP/1/test+vries+1.pdf'
    self.token__Invokerportlet_WAR_ui001SNAPSHOT_INSTANCE_U7mb_windowName = \
      'cb2b1033-c86b-41f0-a1e0-cc8d3dc6b5e5'
    result = request1001.GET('/web/test/home' +
      '?p_p_id=' +
      self.token_p_p_id +
      '&p_p_lifecycle=' +
      self.token_p_p_lifecycle +
      '&p_p_state=' +
      self.token_p_p_state +
      '&p_p_mode=' +
      self.token_p_p_mode +
      '&p_p_resource_id=' +
      self.token_p_p_resource_id +
      '&p_p_cacheability=' +
      self.token_p_p_cacheability +
      '&_Invokerportlet_WAR_ui001SNAPSHOT_INSTANCE_U7mb_windowName=' +
      self.token__Invokerportlet_WAR_ui001SNAPSHOT_INSTANCE_U7mb_windowName)

    return result

  def __call__(self):
    """This method is called for every run performed by the worker thread."""
    self.page1()      # GET home (requests 101-103)

    grinder.sleep(547)
    self.page2()      # POST home (request 201)

    grinder.sleep(2828)
    self.page3()      # POST home (request 301)

    grinder.sleep(1843)
    self.page4()      # POST home (request 401)

    grinder.sleep(2515)
    self.page5()      # POST home (request 501)

    grinder.sleep(4625)
    self.page6()      # POST home (request 601)

    grinder.sleep(1297)
    self.page7()      # POST home (request 701)

    grinder.sleep(1625)
    self.page8()      # POST home (request 801)

    grinder.sleep(1422)
    self.page9()      # POST home (request 901)

    grinder.sleep(94)
    self.page10()     # GET home (request 1001)


def instrumentMethod(test, method_name, c=TestRunner):
  """Instrument a method with the given Test."""
  unadorned = getattr(c, method_name)
  import new
  method = new.instancemethod(test.wrap(unadorned), None, c)
  setattr(c, method_name, method)

# Replace each method with an instrumented version.
# You can call the unadorned method using self.page1.__target__().
instrumentMethod(Test(100, 'Pobranie liferaya'), 'page1')
instrumentMethod(Test(200, 'Przywitanie vaadina'), 'page2')
instrumentMethod(Test(300, 'Page 3'), 'page3')
instrumentMethod(Test(400, 'Page 4'), 'page4')
instrumentMethod(Test(500, 'Page 5'), 'page5')
instrumentMethod(Test(600, 'Page 6'), 'page6')
instrumentMethod(Test(700, 'Page 7'), 'page7')
instrumentMethod(Test(800, 'Page 8'), 'page8')
instrumentMethod(Test(900, 'Generowanie raportu'), 'page9')
instrumentMethod(Test(1000, 'Pobranie raportu'), 'page10')
