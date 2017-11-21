function ptui_qrcode_CB(t) {
    clearTimeout(pt._timer),
    t && 0 == t.ec && pt.qrcode.polling(t.qrcode)
}
function weixin_sig_cb() {
    var t = function() {
        WeixinJSBridge.invoke("getInstallState", {
            packageName: "com.tencent.mobileqq",
            packageUrl: "mqq://"
        }, function(t) {
            var e, n = t && t.err_msg;
            if (n && (e = n.match(/:yes(?:_(\d+))?/))) {
                (pt.isIPhone || pt.isAndroid && e[1] >= 336) && pt.showOneKey("justshow")
            }
        }
        )
    }
    ;
    "object" == typeof WeixinJSBridge && "function" == typeof WeixinJSBridge.invoke ? t() : document.addEventListener ? document.addEventListener("WeixinJSBridgeReady", t, !1) : document.attachEvent && (document.attachEvent("WeixinJSBridgeReady", t),
    document.attachEvent("onWeixinJSBridgeReady", t))
}
function ptui_checkVC(t, e, n, i, o) {
    pt.isloading && (clearTimeout(pt._timer),
    pt.endLoading(),
    pt.cb_checkVC(t, e, n, i, o))
}
function ptui_changeImg() {}
function ptuiCB(t, e, n, i, o, r) {
    if (pt.isloading || pt.qrcode.used) {
        clearTimeout(pt._timer);
        try {
            pt.endLoading()
        } catch (t) {}
        pt.cb(t, e, n, i, o, r)
    }
}
function imgLoadReport() {}
function ptui_checkValidate() {
    return pt.checkValidate()
}
function ptui_auth_CB(t, e) {
    switch (parseInt(t)) {
    case 0:
        pt.isHulian ? pt.setCookieLogin() : pt.showAuth(e);
        break;
    case 1:
        if (pt.isHulian) {
            pt.cancel_cookielogin();
            break
        }
        var n = navigator.userAgent
          , i = n.match(/QQ\/(\d\.\d\.\d)/i);
        if (i && i[1] >= "5.9" && pt.accessCount() < 5) {
            var e = (pt.isHttps ? "https://ssl." : "http://") + "ptlogin2.qq.com/jump?clientuin=$UIN&clientkey=$KEY&keyindex=$KEYINDEX&u1=" + encodeURIComponent(pt.s_url);
            pt.redirect(pt.target, e);
            break
        }
        break;
    case 2:
        if (pt.isHulian) {
            pt.setCookieLogin();
            break
        }
        var o = e + "&regmaster=" + window.ptui_regmaster + "&aid=" + window.ptui_appid + "&s_url=" + encodeURIComponent(pt.s_url);
        1 == pt.low_login_enable && (o += "&low_login_enable=1&low_login_hour=" + window.ptui_low_login_hour),
        "1" == window.ptui_pt_ttype && (o += "&pt_ttype=1"),
        "1" == window.ptui_pt_light && (o += "&pt_light=1"),
        pt.redirect(pt.target, o)
    }
    pt.refreshQloginUI()
}
function ptui_qlogin_CB(t, e, n) {
    switch (t + "") {
    case "0":
        pt.redirect(pt.target, e);
        break;
    case "5":
        if (MTT.refreshToken) {
            var i = setTimeout(function() {
                pt.showErr(n)
            }
            , 3e3);
            MTT.refreshToken(pt.qqBrowserInfo.uin, function(t) {
                MTT.refreshToken = null ,
                t.stweb && ($.report.monitor("624562"),
                clearTimeout(i),
                pt.qqBrowserInfo.loginkey = t.stweb,
                pt.qlogin_submit())
            }
            ),
            $.report.monitor("624561")
        } else
            pt.showErr(n);
        break;
    default:
        $.report.nlog("qq浏览器快速登录失败," + t, "443881", pt.qqBrowserInfo.uin),
        pt.showErr(n)
    }
}
function OneKey(t) {
    if (OneKey.done = !1,
    OneKey.TIMEOUT = 3e3,
    pt.isWX) {
        OneKey.TIMEOUT = 5e3,
        OneKey.qrcode = !0;
        for (var e in OneKey.ERRMSG)
            OneKey.ERRMSG.hasOwnProperty(e) && (OneKey.ERRMSG[e] = OneKey.ERRMSG[e].replace(/<a.*>([^<]*)<\/a>/, "$1"));
        pt.qrcode.get(1)
    } else
        setTimeout(function() {
            openApp(t)
        }
        , 100)
}
function openApp(t, e, n) {
    if (!OneKey.done && !pt.isLaunching) {
        pt.isLaunching = !0;
        var i = OneKey.TIMEOUT
          , o = new Date;
        pt.btnOnekey.innerHTML = STR_LANG.onekeying,
        setTimeout(function() {
            e && e(),
            pt.isLaunching = !1,
            pt.btnOnekey.innerHTML = STR_LANG.onekey,
            pt.qrcode.done || new Date - o <= i + 200 && (n && n(),
            pt.showErr(OneKey.ERRMSG[ptui_lang], 5e3),
            $.report.nlog("callApp failed:" + navigator.userAgent, 424783))
        }
        , i),
        pt.isWX && pt.isAndroid ? setTimeout(function() {
            doOpenApp(t)
        }
        , 100) : doOpenApp(t)
    }
}
function doOpenApp(t, e) {
    var n = $.detectBrowser()
      , i = n[0] && n[0].toLowerCase()
      , o = {};
    if (pt.isWX && !e)
        WeixinJSBridge.invoke("launchApplication", {
            schemeUrl: t
        }, function(e) {
            (e && e.err_msg).match(/launchApplication:ok/) || doOpenApp(t, !0)
        }
        );
    else if (pt.isAndroid) {
        var r = n[1] || "location";
        switch (i && (o = {
            ucbrowser: "ucweb",
            meizu: "mzbrowser",
            liebaofast: "lb"
        },
        o[i] && (t += "&schemacallback=" + encodeURIComponent(o[i] + "://"))),
        r) {
        case "iframe":
            openApp.iframe ? openApp.iframe.src = t : (openApp.iframe = document.createElement("iframe"),
            openApp.iframe.src = t,
            openApp.iframe.style.display = "none",
            document.body.appendChild(openApp.iframe)),
            openApp.flag = "iframe";
            break;
        case "open":
            var a = window.open(t, "_blank");
            setTimeout(function() {
                a.close()
            }
            , 0),
            openApp.flag = "open";
            break;
        case "location":
            location.href = t,
            openApp.flag = "location"
        }
    } else
        i && (o = {
            ucbrowser: "ucbrowser://",
            liebao: "lb://u/100/"
        },
        o[i] && (t += "&schemacallback=" + encodeURIComponent(o[i]))),
        pt.isInIframe ? top.location.href = t : location.href = t,
        openApp.flag = "location"
}
function openSDKCallBack(t) {
    var e = t.result
      , n = t.data
      , i = t.sn;
    switch (i) {
    case 4:
        openSDK.md5Pwd = n,
        openSDK.result = e,
        openSDK.callbackArray[i].call()
    }
}
function get_app_basicinfo(t) {
    pt.open.fillAppInfo(t)
}
function ptui_wtlogin_CB() {
    clearInterval(pt.qrcode.clock),
    pt.isLoading && pt.endLoading(),
    window.wtCB && window.wtCB.apply(window, arguments)
}
var $ = window.Simple = function(t) {
    return "string" == typeof t ? document.getElementById(t) : t
}
;
$.cookie = {
    get: function(t) {
        var e;
        return function(t) {
            if (!t)
                return t;
            for (; t != unescape(t); )
                t = unescape(t);
            for (var e = ["<", ">", "'", '"', "%3c", "%3e", "%27", "%22", "%253c", "%253e", "%2527", "%2522"], n = ["&#x3c;", "&#x3e;", "&#x27;", "&#x22;", "%26%23x3c%3B", "%26%23x3e%3B", "%26%23x27%3B", "%26%23x22%3B", "%2526%2523x3c%253B", "%2526%2523x3e%253B", "%2526%2523x27%253B", "%2526%2523x22%253B"], i = 0; i < e.length; i++)
                t = t.replace(new RegExp(e[i],"gi"), n[i]);
            return t
        }
        ((e = document.cookie.match(RegExp("(^|;\\s*)" + t + "=([^;]*)(;|$)"))) ? unescape(e[2]) : "")
    },
    set: function(t, e, n, i, o) {
        var r = new Date;
        o ? (r.setTime(r.getTime() + 36e5 * o),
        document.cookie = t + "=" + e + "; expires=" + r.toGMTString() + "; path=" + (i || "/") + "; " + (n ? "domain=" + n + ";" : "")) : document.cookie = t + "=" + e + "; path=" + (i || "/") + "; " + (n ? "domain=" + n + ";" : "")
    },
    del: function(t, e, n) {
        document.cookie = t + "=; expires=Mon, 26 Jul 1997 05:00:00 GMT; path=" + (n || "/") + "; " + (e ? "domain=" + e + ";" : "")
    },
    uin: function() {
        var t = $.cookie.get("uin");
        return t ? parseInt(t.substring(1, t.length), 10) : null 
    }
},
$.http = {
    jsonp: function(t) {
        var e = document.createElement("script");
        e.src = t,
        document.getElementsByTagName("head")[0].appendChild(e)
    },
    loadScript: function(t, e, n) {
        var i = document.createElement("script");
        i.onload = i.onreadystatechange = function() {
            this.readyState && "loaded" !== this.readyState && "complete" !== this.readyState || ("function" == typeof e && e(),
            i.onload = i.onreadystatechange = null ,
            i.parentNode && i.parentNode.removeChild(i))
        }
        ,
        i.src = t,
        document.getElementsByTagName("head")[0].appendChild(i)
    },
    ajax: function(url, para, cb, method, type) {
        var xhr = new XMLHttpRequest;
        return xhr.open(method, url),
        xhr.onreadystatechange = function() {
            4 == xhr.readyState && ((xhr.status >= 200 && xhr.status < 300 || 304 === xhr.status || 1223 === xhr.status || 0 === xhr.status) && cb(void 0 === type && xhr.responseText ? eval("(" + xhr.responseText + ")") : xhr.responseText),
            xhr = null )
        }
        ,
        xhr.send(para),
        xhr
    },
    get: function(t, e, n, i) {
        if (e) {
            var o = [];
            for (var r in e)
                e.hasOwnProperty(r) && o.push(r + "=" + e[r]);
            -1 == t.indexOf("?") && (t += "?"),
            t += o.join("&")
        }
        return $.http.ajax(t, null , n, "GET", i)
    },
    preload: function(t) {
        var e = document.createElement("img");
        e.src = t,
        e = null 
    }
},
$.get = $.http.get,
$.post = $.http.post,
$.jsonp = $.http.jsonp,
$.browser = function(t) {
    if (void 0 === $.browser.info) {
        var e = {
            type: ""
        }
          , n = navigator.userAgent.toLowerCase();
        /chrome/.test(n) ? e = {
            type: "chrome",
            version: /chrome[\/ ]([\w.]+)/
        } : /opera/.test(n) ? e = {
            type: "opera",
            version: /version/.test(n) ? /version[\/ ]([\w.]+)/ : /opera[\/ ]([\w.]+)/
        } : /msie/.test(n) ? e = {
            type: "msie",
            version: /msie ([\w.]+)/
        } : /mozilla/.test(n) && !/compatible/.test(n) ? e = {
            type: "ff",
            version: /rv:([\w.]+)/
        } : /safari/.test(n) && (e = {
            type: "safari",
            version: /safari[\/ ]([\w.]+)/
        }),
        e.version = (e.version && e.version.exec(n) || [0, "0"])[1],
        $.browser.info = e
    }
    return $.browser.info[t]
}
,
$.e = {
    _counter: 0,
    _uid: function() {
        return "h" + $.e._counter++
    },
    add: function(t, e, n) {
        if ("object" != typeof t && (t = $(t)),
        document.addEventListener)
            t.addEventListener(e, n, !1);
        else if (document.attachEvent) {
            if (-1 != $.e._find(t, e, n))
                return;
            var i = function(e) {
                e || (e = window.event);
                var i = {
                    _event: e,
                    type: e.type,
                    target: e.srcElement,
                    currentTarget: t,
                    relatedTarget: e.fromElement ? e.fromElement : e.toElement,
                    eventPhase: e.srcElement == t ? 2 : 3,
                    clientX: e.clientX,
                    clientY: e.clientY,
                    screenX: e.screenX,
                    screenY: e.screenY,
                    altKey: e.altKey,
                    ctrlKey: e.ctrlKey,
                    shiftKey: e.shiftKey,
                    keyCode: e.keyCode,
                    data: e.data,
                    origin: e.origin,
                    stopPropagation: function() {
                        this._event.cancelBubble = !0
                    },
                    preventDefault: function() {
                        this._event.returnValue = !1
                    }
                };
                Function.prototype.call ? n.call(t, i) : (t._currentHandler = n,
                t._currentHandler(i),
                t._currentHandler = null )
            }
            ;
            t.attachEvent("on" + e, i);
            var o = {
                element: t,
                eventType: e,
                handler: n,
                wrappedHandler: i
            }
              , r = t.document || t
              , a = r.parentWindow
              , p = $.e._uid();
            a._allHandlers || (a._allHandlers = {}),
            a._allHandlers[p] = o,
            t._handlers || (t._handlers = []),
            t._handlers.push(p),
            a._onunloadHandlerRegistered || (a._onunloadHandlerRegistered = !0,
            a.attachEvent("onunload", $.e._removeAllHandlers))
        }
    },
    remove: function(t, e, n) {
        if (document.addEventListener)
            t.removeEventListener(e, n, !1);
        else if (document.attachEvent) {
            var i = $.e._find(t, e, n);
            if (-1 == i)
                return;
            var o = t.document || t
              , r = o.parentWindow
              , a = t._handlers[i]
              , p = r._allHandlers[a];
            t.detachEvent("on" + e, p.wrappedHandler),
            t._handlers.splice(i, 1),
            delete r._allHandlers[a]
        }
    },
    _find: function(t, e, n) {
        var i = t._handlers;
        if (!i)
            return -1;
        for (var o = t.document || t, r = o.parentWindow, a = i.length - 1; a >= 0; a--) {
            var p = i[a]
              , s = r._allHandlers[p];
            if (s.eventType == e && s.handler == n)
                return a
        }
        return -1
    },
    _removeAllHandlers: function() {
        var t = this;
        for (id in t._allHandlers) {
            var e = t._allHandlers[id];
            e.element.detachEvent("on" + e.eventType, e.wrappedHandler),
            delete t._allHandlers[id]
        }
    },
    src: function(t) {
        return t ? t.target : event.srcElement
    },
    stopPropagation: function(t) {
        t ? t.stopPropagation() : event.cancelBubble = !0
    }
},
$.bom = {
    query: function(t) {
        var e = window.location.search.match(new RegExp("(\\?|&)" + t + "=([^&]*)(&|$)"));
        return e ? decodeURIComponent(e[2]) : ""
    }
},
$.winName = {
    set: function(t, e) {
        var n = window.name || "";
        n.match(new RegExp(";" + t + "=([^;]*)(;|$)")) ? window.name = n.replace(new RegExp(";" + t + "=([^;]*)"), ";" + t + "=" + e) : window.name = n + ";" + t + "=" + e
    },
    get: function(t) {
        var e = window.name || ""
          , n = e.match(new RegExp(";" + t + "=([^;]*)(;|$)"));
        return n ? n[1] : ""
    },
    clear: function(t) {
        var e = window.name || "";
        window.name = e.replace(new RegExp(";" + t + "=([^;]*)"), "")
    }
},
$.localStorage = {
    isSupport: function() {
        try {
            return !!window.localStorage
        } catch (t) {
            return !1
        }
    },
    get: function(t) {
        var e = "";
        try {
            e = window.localStorage.getItem(t)
        } catch (t) {
            e = ""
        }
        return e
    },
    set: function(t, e) {
        try {
            window.localStorage.setItem(t, e)
        } catch (t) {}
    },
    remove: function(t) {
        try {
            window.localStorage.removeItem(t)
        } catch (t) {}
    }
},
$.str = function() {
    var htmlDecodeDict = {
        quot: '"',
        lt: "<",
        gt: ">",
        amp: "&",
        nbsp: " ",
        "#34": '"',
        "#60": "<",
        "#62": ">",
        "#38": "&",
        "#160": " "
    }
      , htmlEncodeDict = {
        '"': "#34",
        "<": "#60",
        ">": "#62",
        "&": "#38",
        " ": "#160"
    };
    return {
        decodeHtml: function(t) {
            return t += "",
            t.replace(/&(quot|lt|gt|amp|nbsp);/gi, function(t, e) {
                return htmlDecodeDict[e]
            }
            ).replace(/&#u([a-f\d]{4});/gi, function(t, e) {
                return String.fromCharCode(parseInt("0x" + e))
            }
            ).replace(/&#(\d+);/gi, function(t, e) {
                return String.fromCharCode(+e)
            }
            )
        },
        encodeHtml: function(t) {
            return t += "",
            t.replace(/["<>& ]/g, function(t) {
                return "&" + htmlEncodeDict[t] + ";"
            }
            )
        },
        trim: function(t) {
            t += "";
            for (var t = t.replace(/^\s+/, ""), e = /\s/, n = t.length; e.test(t.charAt(--n)); )
                ;
            return t.slice(0, n + 1)
        },
        uin2hex: function(str) {
            var maxLength = 16;
            str = parseInt(str);
            for (var hex = str.toString(16), len = hex.length, i = len; i < maxLength; i++)
                hex = "0" + hex;
            for (var arr = [], j = 0; j < maxLength; j += 2)
                arr.push("\\x" + hex.substr(j, 2));
            var result = arr.join("");
            return eval('result="' + result + '"'),
            result
        },
        bin2String: function(t) {
            for (var e = [], n = 0, i = t.length; n < i; n++) {
                var o = t.charCodeAt(n).toString(16);
                1 == o.length && (o = "0" + o),
                e.push(o)
            }
            return e = "0x" + e.join(""),
            e = parseInt(e, 16)
        },
        utf8ToUincode: function(t) {
            var e = "";
            try {
                var n = t.length
                  , o = [];
                for (i = 0; i < n; i += 2)
                    o.push("%" + t.substr(i, 2));
                e = decodeURIComponent(o.join("")),
                e = $.str.decodeHtml(e)
            } catch (t) {
                e = ""
            }
            return e
        },
        json2str: function(t) {
            var e = "";
            if ("undefined" != typeof JSON)
                e = JSON.stringify(t);
            else {
                var n = [];
                for (var i in t)
                    n.push("'" + i + "':'" + t[i] + "'");
                e = "{" + n.join(",") + "}"
            }
            return e
        },
        time33: function(t) {
            for (var e = 0, n = 0, i = t.length; n < i; n++)
                e = 33 * e + t.charCodeAt(n);
            return e % 4294967296
        },
        hash33: function(t) {
            for (var e = 0, n = 0, i = t.length; n < i; ++n)
                e += (e << 5) + t.charCodeAt(n);
            return 2147483647 & e
        }
    }
}
(),
$.css = function() {
    return {
        show: function(t) {
            "string" == typeof t && (t = $(t)),
            t.style.display = "block"
        },
        hide: function(t) {
            "string" == typeof t && (t = $(t)),
            t.style.display = "none"
        },
        getElementViewTop: function(t) {
            for (var e = $(t), n = e.offsetTop, i = e.offsetParent; null  !== i; )
                n += i.offsetTop,
                i = i.offsetParent;
            if ("BackCompat" == document.compatMode)
                var o = document.body.scrollTop;
            else
                var o = document.documentElement.scrollTop;
            return n - o
        }
    }
}
(),
$.check = {
    isHttps: function() {
        return "https:" == document.location.protocol
    },
    isSsl: function() {
        return /^ssl./i.test(document.location.host)
    },
    isIpad: function() {
        return /ipad/i.test(navigator.userAgent.toLowerCase())
    },
    isQQ: function(t) {
        return /^[1-9]{1}\d{4,9}$/.test(t)
    },
    isNullQQ: function(t) {
        return /^\d{1,4}$/.test(t)
    },
    isNick: function(t) {
        return /^[a-zA-Z]{1}([a-zA-Z0-9]|[-_]){0,19}$/.test(t)
    },
    isName: function(t) {
        return "<请输入帐号>" != t && /[\u4E00-\u9FA5]{1,8}/.test(t)
    },
    isPhone: function(t) {
        return /^(?:86|886|)1\d{10}\s*$/.test(t)
    },
    isDXPhone: function(t) {
        return /^(?:86|886|)1(?:33|53|80|81|89)\d{8}$/.test(t)
    },
    isSeaPhone: function(t) {
        return /^(00)?(?:852|853|886(0)?\d{1})\d{8}$/.test(t)
    },
    isMail: function(t) {
        return /^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/.test(t)
    },
    isPassword: function(t) {
        return t && t.length >= 16
    },
    isForeignPhone: function(t) {
        return /^00\d{7,}/.test(t)
    },
    needVip: function(t) {
        for (var e = ["21001601", "21000110", "21000121", "46000101", "716027609", "716027610", "549000912", "717016513"], n = !0, i = 0, o = e.length; i < o; i++)
            if (e[i] == t) {
                n = !1;
                break
            }
        return n
    },
    isPaipai: function() {
        return /paipai.com$/.test(window.location.hostname)
    },
    isPaipaiDuokefu: function(t) {
        return /^.+@.+$/.test(t)
    },
    is_weibo_appid: function(t) {
        return 46000101 == t || 607000101 == t || 558032501 == t || 682023901 == t
    }
},
$.report = {
    monitor: function(t, e) {
        if (!(Math.random() > (e || 1))) {
            var n = location.protocol + "//ui.ptlogin2.qq.com/cgi-bin/report?id=" + t;
            $.http.preload(n)
        }
    },
    nlog: function(t, e, n) {
        var i = "//ui.ptlogin2.qq.com/cgi-bin/report?"
          , o = encodeURIComponent(t + "|_|" + location.href + "|_|" + window.navigator.userAgent);
        e = e || 0,
        n && (i += "u=" + n + "&"),
        i += "id=" + e + "&msg=" + o + "&v=" + Math.random(),
        $.http.preload(i)
    },
    log: function(t) {
        $.http.preload("http://console.log?msg=" + encodeURIComponent("string" == typeof t ? t : JSON.stringify(t)))
    }
},
$.detectBrowser = function() {
    if (window.MzJavascriptInterface && "function" == typeof window.MzJavascriptInterface.isMzBrowser && window.MzJavascriptInterface.isMzBrowser())
        return ["meizu", "location"];
    var t, e = navigator.userAgent;
    if (/android/i.test(e)) {
        if (t = e.match(/MQQBrowser|UCBrowser|360Browser|Firefox|MicroMessenger/i))
            t[1] = "location";
        else if (t = e.match(/baidubrowse|SogouMobileBrowser|LieBaoFast|XiaoMi\/MiuiBrowser|opr|vivo/i))
            t[1] = "iframe";
        else if (t = e.match(/Chrome/i)) {
            var n = e.match(/chrome\/([\d]+)/i);
            n && (n = n[1]),
            40 != n && (t[1] = "open")
        }
    } else
        /iphone|ipod/gi.test(e) && ((t = e.match(/MQQBrowser|UCBrowser|baidubrowse|Opera|360Browser|LieBao/i)) || (t = e.match(/CriOS|Chrome/i)) && "crios" == t[0].toLowerCase() && (t[0] = "Chrome"));
    return t || ["others", ""]
}
,
function() {
    "" != $.cookie.get("nohost_guid") && $.http.loadScript("/nohost_htdocs/js/SwitchHost.js", function() {
        var t = window.SwitchHost && window.SwitchHost.init;
        t && t()
    }
    )
}
(),
setTimeout(function() {
    var t = "http://isdspeed.qq.com/cgi-bin/r.cgi?";
    $.check.isHttps() && (t = "https://huatuospeed.weiyun.com/cgi-bin/r.cgi?"),
    t += "flag1=7808&flag2=1&flag3=9";
    Math.random() < .01 && (void 0 !== window.postMessage ? t += "&2=2000" : t += "&2=1000",
    t += "&v=" + Math.random(),
    $.http.preload(t))
}
, 500),
$ = window.$ || {},
$pt = window.$pt || {},
$.RSA = $pt.RSA = function() {
    function t(t, e) {
        return new a(t,e)
    }
    function e(t, e) {
        if (e < t.length + 11)
            return uv_alert("Message too long for RSA"),
            null ;
        for (var n = new Array, i = t.length - 1; i >= 0 && e > 0; ) {
            var o = t.charCodeAt(i--);
            n[--e] = o
        }
        n[--e] = 0;
        for (var r = new Y, p = new Array; e > 2; ) {
            for (p[0] = 0; 0 == p[0]; )
                r.nextBytes(p);
            n[--e] = p[0]
        }
        return n[--e] = 2,
        n[--e] = 0,
        new a(n)
    }
    function n() {
        this.n = null ,
        this.e = 0,
        this.d = null ,
        this.p = null ,
        this.q = null ,
        this.dmp1 = null ,
        this.dmq1 = null ,
        this.coeff = null 
    }
    function i(e, n) {
        null  != e && null  != n && e.length > 0 && n.length > 0 ? (this.n = t(e, 16),
        this.e = parseInt(n, 16)) : uv_alert("Invalid RSA public key")
    }
    function o(t) {
        return t.modPowInt(this.e, this.n)
    }
    function r(t) {
        var n = e(t, this.n.bitLength() + 7 >> 3);
        if (null  == n)
            return null ;
        var i = this.doPublic(n);
        if (null  == i)
            return null ;
        var o = i.toString(16);
        return 0 == (1 & o.length) ? o : "0" + o
    }
    function a(t, e, n) {
        null  != t && ("number" == typeof t ? this.fromNumber(t, e, n) : null  == e && "string" != typeof t ? this.fromString(t, 256) : this.fromString(t, e))
    }
    function p() {
        return new a(null )
    }
    function s(t, e, n, i, o, r) {
        for (; --r >= 0; ) {
            var a = e * this[t++] + n[i] + o;
            o = Math.floor(a / 67108864),
            n[i++] = 67108863 & a
        }
        return o
    }
    function c(t, e, n, i, o, r) {
        for (var a = 32767 & e, p = e >> 15; --r >= 0; ) {
            var s = 32767 & this[t]
              , c = this[t++] >> 15
              , u = p * s + c * a;
            s = a * s + ((32767 & u) << 15) + n[i] + (1073741823 & o),
            o = (s >>> 30) + (u >>> 15) + p * c + (o >>> 30),
            n[i++] = 1073741823 & s
        }
        return o
    }
    function u(t, e, n, i, o, r) {
        for (var a = 16383 & e, p = e >> 14; --r >= 0; ) {
            var s = 16383 & this[t]
              , c = this[t++] >> 14
              , u = p * s + c * a;
            s = a * s + ((16383 & u) << 14) + n[i] + o,
            o = (s >> 28) + (u >> 14) + p * c,
            n[i++] = 268435455 & s
        }
        return o
    }
    function l(t) {
        return st.charAt(t)
    }
    function d(t, e) {
        var n = ct[t.charCodeAt(e)];
        return null  == n ? -1 : n
    }
    function f(t) {
        for (var e = this.t - 1; e >= 0; --e)
            t[e] = this[e];
        t.t = this.t,
        t.s = this.s
    }
    function h(t) {
        this.t = 1,
        this.s = t < 0 ? -1 : 0,
        t > 0 ? this[0] = t : t < -1 ? this[0] = t + DV : this.t = 0
    }
    function g(t) {
        var e = p();
        return e.fromInt(t),
        e
    }
    function w(t, e) {
        var n;
        if (16 == e)
            n = 4;
        else if (8 == e)
            n = 3;
        else if (256 == e)
            n = 8;
        else if (2 == e)
            n = 1;
        else if (32 == e)
            n = 5;
        else {
            if (4 != e)
                return void this.fromRadix(t, e);
            n = 2
        }
        this.t = 0,
        this.s = 0;
        for (var i = t.length, o = !1, r = 0; --i >= 0; ) {
            var p = 8 == n ? 255 & t[i] : d(t, i);
            p < 0 ? "-" == t.charAt(i) && (o = !0) : (o = !1,
            0 == r ? this[this.t++] = p : r + n > this.DB ? (this[this.t - 1] |= (p & (1 << this.DB - r) - 1) << r,
            this[this.t++] = p >> this.DB - r) : this[this.t - 1] |= p << r,
            (r += n) >= this.DB && (r -= this.DB))
        }
        8 == n && 0 != (128 & t[0]) && (this.s = -1,
        r > 0 && (this[this.t - 1] |= (1 << this.DB - r) - 1 << r)),
        this.clamp(),
        o && a.ZERO.subTo(this, this)
    }
    function m() {
        for (var t = this.s & this.DM; this.t > 0 && this[this.t - 1] == t; )
            --this.t
    }
    function _(t) {
        if (this.s < 0)
            return "-" + this.negate().toString(t);
        var e;
        if (16 == t)
            e = 4;
        else if (8 == t)
            e = 3;
        else if (2 == t)
            e = 1;
        else if (32 == t)
            e = 5;
        else {
            if (4 != t)
                return this.toRadix(t);
            e = 2
        }
        var n, i = (1 << e) - 1, o = !1, r = "", a = this.t, p = this.DB - a * this.DB % e;
        if (a-- > 0)
            for (p < this.DB && (n = this[a] >> p) > 0 && (o = !0,
            r = l(n)); a >= 0; )
                p < e ? (n = (this[a] & (1 << p) - 1) << e - p,
                n |= this[--a] >> (p += this.DB - e)) : (n = this[a] >> (p -= e) & i,
                p <= 0 && (p += this.DB,
                --a)),
                n > 0 && (o = !0),
                o && (r += l(n));
        return o ? r : "0"
    }
    function v() {
        var t = p();
        return a.ZERO.subTo(this, t),
        t
    }
    function y() {
        return this.s < 0 ? this.negate() : this
    }
    function b(t) {
        var e = this.s - t.s;
        if (0 != e)
            return e;
        var n = this.t;
        if (0 != (e = n - t.t))
            return e;
        for (; --n >= 0; )
            if (0 != (e = this[n] - t[n]))
                return e;
        return 0
    }
    function k(t) {
        var e, n = 1;
        return 0 != (e = t >>> 16) && (t = e,
        n += 16),
        0 != (e = t >> 8) && (t = e,
        n += 8),
        0 != (e = t >> 4) && (t = e,
        n += 4),
        0 != (e = t >> 2) && (t = e,
        n += 2),
        0 != (e = t >> 1) && (t = e,
        n += 1),
        n
    }
    function $() {
        return this.t <= 0 ? 0 : this.DB * (this.t - 1) + k(this[this.t - 1] ^ this.s & this.DM)
    }
    function q(t, e) {
        var n;
        for (n = this.t - 1; n >= 0; --n)
            e[n + t] = this[n];
        for (n = t - 1; n >= 0; --n)
            e[n] = 0;
        e.t = this.t + t,
        e.s = this.s
    }
    function S(t, e) {
        for (var n = t; n < this.t; ++n)
            e[n - t] = this[n];
        e.t = Math.max(this.t - t, 0),
        e.s = this.s
    }
    function T(t, e) {
        var n, i = t % this.DB, o = this.DB - i, r = (1 << o) - 1, a = Math.floor(t / this.DB), p = this.s << i & this.DM;
        for (n = this.t - 1; n >= 0; --n)
            e[n + a + 1] = this[n] >> o | p,
            p = (this[n] & r) << i;
        for (n = a - 1; n >= 0; --n)
            e[n] = 0;
        e[a] = p,
        e.t = this.t + a + 1,
        e.s = this.s,
        e.clamp()
    }
    function I(t, e) {
        e.s = this.s;
        var n = Math.floor(t / this.DB);
        if (n >= this.t)
            return void (e.t = 0);
        var i = t % this.DB
          , o = this.DB - i
          , r = (1 << i) - 1;
        e[0] = this[n] >> i;
        for (var a = n + 1; a < this.t; ++a)
            e[a - n - 1] |= (this[a] & r) << o,
            e[a - n] = this[a] >> i;
        i > 0 && (e[this.t - n - 1] |= (this.s & r) << o),
        e.t = this.t - n,
        e.clamp()
    }
    function A(t, e) {
        for (var n = 0, i = 0, o = Math.min(t.t, this.t); n < o; )
            i += this[n] - t[n],
            e[n++] = i & this.DM,
            i >>= this.DB;
        if (t.t < this.t) {
            for (i -= t.s; n < this.t; )
                i += this[n],
                e[n++] = i & this.DM,
                i >>= this.DB;
            i += this.s
        } else {
            for (i += this.s; n < t.t; )
                i -= t[n],
                e[n++] = i & this.DM,
                i >>= this.DB;
            i -= t.s
        }
        e.s = i < 0 ? -1 : 0,
        i < -1 ? e[n++] = this.DV + i : i > 0 && (e[n++] = i),
        e.t = n,
        e.clamp()
    }
    function C(t, e) {
        var n = this.abs()
          , i = t.abs()
          , o = n.t;
        for (e.t = o + i.t; --o >= 0; )
            e[o] = 0;
        for (o = 0; o < i.t; ++o)
            e[o + n.t] = n.am(0, i[o], e, o, 0, n.t);
        e.s = 0,
        e.clamp(),
        this.s != t.s && a.ZERO.subTo(e, e)
    }
    function E(t) {
        for (var e = this.abs(), n = t.t = 2 * e.t; --n >= 0; )
            t[n] = 0;
        for (n = 0; n < e.t - 1; ++n) {
            var i = e.am(n, e[n], t, 2 * n, 0, 1);
            (t[n + e.t] += e.am(n + 1, 2 * e[n], t, 2 * n + 1, i, e.t - n - 1)) >= e.DV && (t[n + e.t] -= e.DV,
            t[n + e.t + 1] = 1)
        }
        t.t > 0 && (t[t.t - 1] += e.am(n, e[n], t, 2 * n, 0, 1)),
        t.s = 0,
        t.clamp()
    }
    function D(t, e, n) {
        var i = t.abs();
        if (!(i.t <= 0)) {
            var o = this.abs();
            if (o.t < i.t)
                return null  != e && e.fromInt(0),
                void (null  != n && this.copyTo(n));
            null  == n && (n = p());
            var r = p()
              , s = this.s
              , c = t.s
              , u = this.DB - k(i[i.t - 1]);
            u > 0 ? (i.lShiftTo(u, r),
            o.lShiftTo(u, n)) : (i.copyTo(r),
            o.copyTo(n));
            var l = r.t
              , d = r[l - 1];
            if (0 != d) {
                var f = d * (1 << this.F1) + (l > 1 ? r[l - 2] >> this.F2 : 0)
                  , h = this.FV / f
                  , g = (1 << this.F1) / f
                  , w = 1 << this.F2
                  , m = n.t
                  , _ = m - l
                  , v = null  == e ? p() : e;
                for (r.dlShiftTo(_, v),
                n.compareTo(v) >= 0 && (n[n.t++] = 1,
                n.subTo(v, n)),
                a.ONE.dlShiftTo(l, v),
                v.subTo(r, r); r.t < l; )
                    r[r.t++] = 0;
                for (; --_ >= 0; ) {
                    var y = n[--m] == d ? this.DM : Math.floor(n[m] * h + (n[m - 1] + w) * g);
                    if ((n[m] += r.am(0, y, n, _, 0, l)) < y)
                        for (r.dlShiftTo(_, v),
                        n.subTo(v, n); n[m] < --y; )
                            n.subTo(v, n)
                }
                null  != e && (n.drShiftTo(l, e),
                s != c && a.ZERO.subTo(e, e)),
                n.t = l,
                n.clamp(),
                u > 0 && n.rShiftTo(u, n),
                s < 0 && a.ZERO.subTo(n, n)
            }
        }
    }
    function L(t) {
        var e = p();
        return this.abs().divRemTo(t, null , e),
        this.s < 0 && e.compareTo(a.ZERO) > 0 && t.subTo(e, e),
        e
    }
    function M(t) {
        this.m = t
    }
    function x(t) {
        return t.s < 0 || t.compareTo(this.m) >= 0 ? t.mod(this.m) : t
    }
    function B(t) {
        return t
    }
    function O(t) {
        t.divRemTo(this.m, null , t)
    }
    function R(t, e, n) {
        t.multiplyTo(e, n),
        this.reduce(n)
    }
    function H(t, e) {
        t.squareTo(e),
        this.reduce(e)
    }
    function K() {
        if (this.t < 1)
            return 0;
        var t = this[0];
        if (0 == (1 & t))
            return 0;
        var e = 3 & t;
        return e = e * (2 - (15 & t) * e) & 15,
        e = e * (2 - (255 & t) * e) & 255,
        e = e * (2 - ((65535 & t) * e & 65535)) & 65535,
        e = e * (2 - t * e % this.DV) % this.DV,
        e > 0 ? this.DV - e : -e
    }
    function U(t) {
        this.m = t,
        this.mp = t.invDigit(),
        this.mpl = 32767 & this.mp,
        this.mph = this.mp >> 15,
        this.um = (1 << t.DB - 15) - 1,
        this.mt2 = 2 * t.t
    }
    function N(t) {
        var e = p();
        return t.abs().dlShiftTo(this.m.t, e),
        e.divRemTo(this.m, null , e),
        t.s < 0 && e.compareTo(a.ZERO) > 0 && this.m.subTo(e, e),
        e
    }
    function P(t) {
        var e = p();
        return t.copyTo(e),
        this.reduce(e),
        e
    }
    function j(t) {
        for (; t.t <= this.mt2; )
            t[t.t++] = 0;
        for (var e = 0; e < this.m.t; ++e) {
            var n = 32767 & t[e]
              , i = n * this.mpl + ((n * this.mph + (t[e] >> 15) * this.mpl & this.um) << 15) & t.DM;
            for (n = e + this.m.t,
            t[n] += this.m.am(0, i, t, e, 0, this.m.t); t[n] >= t.DV; )
                t[n] -= t.DV,
                t[++n]++
        }
        t.clamp(),
        t.drShiftTo(this.m.t, t),
        t.compareTo(this.m) >= 0 && t.subTo(this.m, t)
    }
    function Q(t, e) {
        t.squareTo(e),
        this.reduce(e)
    }
    function V(t, e, n) {
        t.multiplyTo(e, n),
        this.reduce(n)
    }
    function F() {
        return 0 == (this.t > 0 ? 1 & this[0] : this.s)
    }
    function z(t, e) {
        if (t > 4294967295 || t < 1)
            return a.ONE;
        var n = p()
          , i = p()
          , o = e.convert(this)
          , r = k(t) - 1;
        for (o.copyTo(n); --r >= 0; )
            if (e.sqrTo(n, i),
            (t & 1 << r) > 0)
                e.mulTo(i, o, n);
            else {
                var s = n;
                n = i,
                i = s
            }
        return e.revert(n)
    }
    function J(t, e) {
        var n;
        return n = t < 256 || e.isEven() ? new M(e) : new U(e),
        this.exp(t, n)
    }
    function G(t) {
        lt[dt++] ^= 255 & t,
        lt[dt++] ^= t >> 8 & 255,
        lt[dt++] ^= t >> 16 & 255,
        lt[dt++] ^= t >> 24 & 255,
        dt >= gt && (dt -= gt)
    }
    function W() {
        G((new Date).getTime())
    }
    function X() {
        if (null  == ut) {
            for (W(),
            ut = it(),
            ut.init(lt),
            dt = 0; dt < lt.length; ++dt)
                lt[dt] = 0;
            dt = 0
        }
        return ut.next()
    }
    function Z(t) {
        var e;
        for (e = 0; e < t.length; ++e)
            t[e] = X()
    }
    function Y() {}
    function tt() {
        this.i = 0,
        this.j = 0,
        this.S = new Array
    }
    function et(t) {
        var e, n, i;
        for (e = 0; e < 256; ++e)
            this.S[e] = e;
        for (n = 0,
        e = 0; e < 256; ++e)
            n = n + this.S[e] + t[e % t.length] & 255,
            i = this.S[e],
            this.S[e] = this.S[n],
            this.S[n] = i;
        this.i = 0,
        this.j = 0
    }
    function nt() {
        var t;
        return this.i = this.i + 1 & 255,
        this.j = this.j + this.S[this.i] & 255,
        t = this.S[this.i],
        this.S[this.i] = this.S[this.j],
        this.S[this.j] = t,
        this.S[t + this.S[this.i] & 255]
    }
    function it() {
        return new tt
    }
    function ot(t, e, i) {
        e = "e9a815ab9d6e86abbf33a4ac64e9196d5be44a09bd0ed6ae052914e1a865ac8331fed863de8ea697e9a7f63329e5e23cda09c72570f46775b7e39ea9670086f847d3c9c51963b131409b1e04265d9747419c635404ca651bbcbc87f99b8008f7f5824653e3658be4ba73e4480156b390bb73bc1f8b33578e7a4e12440e9396f2552c1aff1c92e797ebacdc37c109ab7bce2367a19c56a033ee04534723cc2558cb27368f5b9d32c04d12dbd86bbd68b1d99b7c349a8453ea75d1b2e94491ab30acf6c46a36a75b721b312bedf4e7aad21e54e9bcbcf8144c79b6e3c05eb4a1547750d224c0085d80e6da3907c3d945051c13c7c1dcefd6520ee8379c4f5231ed",
        i = "10001";
        var o = new n;
        return o.setPublic(e, i),
        o.encrypt(t)
    }
    n.prototype.doPublic = o,
    n.prototype.setPublic = i,
    n.prototype.encrypt = r;
    var rt;
    "Microsoft Internet Explorer" == navigator.appName ? (a.prototype.am = c,
    rt = 30) : "Netscape" != navigator.appName ? (a.prototype.am = s,
    rt = 26) : (a.prototype.am = u,
    rt = 28),
    a.prototype.DB = rt,
    a.prototype.DM = (1 << rt) - 1,
    a.prototype.DV = 1 << rt;
    a.prototype.FV = Math.pow(2, 52),
    a.prototype.F1 = 52 - rt,
    a.prototype.F2 = 2 * rt - 52;
    var at, pt, st = "0123456789abcdefghijklmnopqrstuvwxyz", ct = new Array;
    for (at = "0".charCodeAt(0),
    pt = 0; pt <= 9; ++pt)
        ct[at++] = pt;
    for (at = "a".charCodeAt(0),
    pt = 10; pt < 36; ++pt)
        ct[at++] = pt;
    for (at = "A".charCodeAt(0),
    pt = 10; pt < 36; ++pt)
        ct[at++] = pt;
    M.prototype.convert = x,
    M.prototype.revert = B,
    M.prototype.reduce = O,
    M.prototype.mulTo = R,
    M.prototype.sqrTo = H,
    U.prototype.convert = N,
    U.prototype.revert = P,
    U.prototype.reduce = j,
    U.prototype.mulTo = V,
    U.prototype.sqrTo = Q,
    a.prototype.copyTo = f,
    a.prototype.fromInt = h,
    a.prototype.fromString = w,
    a.prototype.clamp = m,
    a.prototype.dlShiftTo = q,
    a.prototype.drShiftTo = S,
    a.prototype.lShiftTo = T,
    a.prototype.rShiftTo = I,
    a.prototype.subTo = A,
    a.prototype.multiplyTo = C,
    a.prototype.squareTo = E,
    a.prototype.divRemTo = D,
    a.prototype.invDigit = K,
    a.prototype.isEven = F,
    a.prototype.exp = z,
    a.prototype.toString = _,
    a.prototype.negate = v,
    a.prototype.abs = y,
    a.prototype.compareTo = b,
    a.prototype.bitLength = $,
    a.prototype.mod = L,
    a.prototype.modPowInt = J,
    a.ZERO = g(0),
    a.ONE = g(1);
    var ut, lt, dt;
    if (null  == lt) {
        lt = new Array,
        dt = 0;
        var ft;
        if ("Netscape" == navigator.appName && navigator.appVersion < "5" && window.crypto && window.crypto.random) {
            var ht = window.crypto.random(32);
            for (ft = 0; ft < ht.length; ++ft)
                lt[dt++] = 255 & ht.charCodeAt(ft)
        }
        for (; dt < gt; )
            ft = Math.floor(65536 * Math.random()),
            lt[dt++] = ft >>> 8,
            lt[dt++] = 255 & ft;
        dt = 0,
        W()
    }
    Y.prototype.nextBytes = Z,
    tt.prototype.init = et,
    tt.prototype.next = nt;
    var gt = 256;
    return {
        rsa_encrypt: ot
    }
}
(),
function(t) {
    function e() {
        return Math.round(4294967295 * Math.random())
    }
    function n(t, e, n) {
        (!n || n > 4) && (n = 4);
        for (var i = 0, o = e; o < e + n; o++)
            i <<= 8,
            i |= t[o];
        return (4294967295 & i) >>> 0
    }
    function i(t, e, n) {
        t[e + 3] = n >> 0 & 255,
        t[e + 2] = n >> 8 & 255,
        t[e + 1] = n >> 16 & 255,
        t[e + 0] = n >> 24 & 255
    }
    function o(t) {
        if (!t)
            return "";
        for (var e = "", n = 0; n < t.length; n++) {
            var i = Number(t[n]).toString(16);
            1 == i.length && (i = "0" + i),
            e += i
        }
        return e
    }
    function r(t) {
        for (var e = "", n = 0; n < t.length; n += 2)
            e += String.fromCharCode(parseInt(t.substr(n, 2), 16));
        return e
    }
    function a(t, e) {
        if (!t)
            return "";
        e && (t = p(t));
        for (var n = [], i = 0; i < t.length; i++)
            n[i] = t.charCodeAt(i);
        return o(n)
    }
    function p(t) {
        var e, n, i = [], o = t.length;
        for (e = 0; e < o; e++)
            n = t.charCodeAt(e),
            n > 0 && n <= 127 ? i.push(t.charAt(e)) : n >= 128 && n <= 2047 ? i.push(String.fromCharCode(192 | n >> 6 & 31), String.fromCharCode(128 | 63 & n)) : n >= 2048 && n <= 65535 && i.push(String.fromCharCode(224 | n >> 12 & 15), String.fromCharCode(128 | n >> 6 & 63), String.fromCharCode(128 | 63 & n));
        return i.join("")
    }
    function s(t) {
        m = new Array(8),
        _ = new Array(8),
        v = y = 0,
        $ = !0,
        w = 0;
        var n = t.length
          , i = 0;
        w = (n + 10) % 8,
        0 != w && (w = 8 - w),
        b = new Array(n + w + 10),
        m[0] = 255 & (248 & e() | w);
        for (var o = 1; o <= w; o++)
            m[o] = 255 & e();
        w++;
        for (var o = 0; o < 8; o++)
            _[o] = 0;
        for (i = 1; i <= 2; )
            w < 8 && (m[w++] = 255 & e(),
            i++),
            8 == w && u();
        for (var o = 0; n > 0; )
            w < 8 && (m[w++] = t[o++],
            n--),
            8 == w && u();
        for (i = 1; i <= 7; )
            w < 8 && (m[w++] = 0,
            i++),
            8 == w && u();
        return b
    }
    function c(t) {
        var e = 0
          , n = new Array(8)
          , i = t.length;
        if (k = t,
        i % 8 != 0 || i < 16)
            return null ;
        if (_ = d(t),
        w = 7 & _[0],
        (e = i - w - 10) < 0)
            return null ;
        for (var o = 0; o < n.length; o++)
            n[o] = 0;
        b = new Array(e),
        y = 0,
        v = 8,
        w++;
        for (var r = 1; r <= 2; )
            if (w < 8 && (w++,
            r++),
            8 == w && (n = t,
            !f()))
                return null ;
        for (var o = 0; 0 != e; )
            if (w < 8 && (b[o] = 255 & (n[y + w] ^ _[w]),
            o++,
            e--,
            w++),
            8 == w && (n = t,
            y = v - 8,
            !f()))
                return null ;
        for (r = 1; r < 8; r++) {
            if (w < 8) {
                if (0 != (n[y + w] ^ _[w]))
                    return null ;
                w++
            }
            if (8 == w && (n = t,
            y = v,
            !f()))
                return null 
        }
        return b
    }
    function u() {
        for (var t = 0; t < 8; t++)
            m[t] ^= $ ? _[t] : b[y + t];
        for (var e = l(m), t = 0; t < 8; t++)
            b[v + t] = e[t] ^ _[t],
            _[t] = m[t];
        y = v,
        v += 8,
        w = 0,
        $ = !1
    }
    function l(t) {
        for (var e = 16, o = n(t, 0, 4), r = n(t, 4, 4), a = n(g, 0, 4), p = n(g, 4, 4), s = n(g, 8, 4), c = n(g, 12, 4), u = 0; e-- > 0; )
            u += 2654435769,
            u = (4294967295 & u) >>> 0,
            o += (r << 4) + a ^ r + u ^ (r >>> 5) + p,
            o = (4294967295 & o) >>> 0,
            r += (o << 4) + s ^ o + u ^ (o >>> 5) + c,
            r = (4294967295 & r) >>> 0;
        var l = new Array(8);
        return i(l, 0, o),
        i(l, 4, r),
        l
    }
    function d(t) {
        for (var e = 16, o = n(t, 0, 4), r = n(t, 4, 4), a = n(g, 0, 4), p = n(g, 4, 4), s = n(g, 8, 4), c = n(g, 12, 4), u = 3816266640; e-- > 0; )
            r -= (o << 4) + s ^ o + u ^ (o >>> 5) + c,
            r = (4294967295 & r) >>> 0,
            o -= (r << 4) + a ^ r + u ^ (r >>> 5) + p,
            o = (4294967295 & o) >>> 0,
            u -= 2654435769,
            u = (4294967295 & u) >>> 0;
        var l = new Array(8);
        return i(l, 0, o),
        i(l, 4, r),
        l
    }
    function f() {
        for (var t = (k.length,
        0); t < 8; t++)
            _[t] ^= k[v + t];
        return _ = d(_),
        v += 8,
        w = 0,
        !0
    }
    function h(t, e) {
        var n = [];
        if (e)
            for (var i = 0; i < t.length; i++)
                n[i] = 255 & t.charCodeAt(i);
        else
            for (var o = 0, i = 0; i < t.length; i += 2)
                n[o++] = parseInt(t.substr(i, 2), 16);
        return n
    }
    var g = ""
      , w = 0
      , m = []
      , _ = []
      , v = 0
      , y = 0
      , b = []
      , k = []
      , $ = !0;
    t.TEA = {
        encrypt: function(t, e) {
            return o(s(h(t, e)))
        },
        enAsBase64: function(t, e) {
            for (var n = h(t, e), i = s(n), o = "", r = 0; r < i.length; r++)
                o += String.fromCharCode(i[r]);
            return btoa(o)
        },
        decrypt: function(t) {
            return o(c(h(t, !1)))
        },
        initkey: function(t, e) {
            g = h(t, e)
        },
        bytesToStr: r,
        strToBytes: a,
        bytesInStr: o,
        dataFromStr: h
    };
    var q = {};
    q.PADCHAR = "=",
    q.ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
    q.getbyte = function(t, e) {
        var n = t.charCodeAt(e);
        if (n > 255)
            throw "INVALID_CHARACTER_ERR: DOM Exception 5";
        return n
    }
    ,
    q.encode = function(t) {
        if (1 != arguments.length)
            throw "SyntaxError: Not enough arguments";
        var e, n, i = q.PADCHAR, o = q.ALPHA, r = q.getbyte, a = [];
        t = "" + t;
        var p = t.length - t.length % 3;
        if (0 == t.length)
            return t;
        for (e = 0; e < p; e += 3)
            n = r(t, e) << 16 | r(t, e + 1) << 8 | r(t, e + 2),
            a.push(o.charAt(n >> 18)),
            a.push(o.charAt(n >> 12 & 63)),
            a.push(o.charAt(n >> 6 & 63)),
            a.push(o.charAt(63 & n));
        switch (t.length - p) {
        case 1:
            n = r(t, e) << 16,
            a.push(o.charAt(n >> 18) + o.charAt(n >> 12 & 63) + i + i);
            break;
        case 2:
            n = r(t, e) << 16 | r(t, e + 1) << 8,
            a.push(o.charAt(n >> 18) + o.charAt(n >> 12 & 63) + o.charAt(n >> 6 & 63) + i)
        }
        return a.join("")
    }
    ,
    window.btoa || (window.btoa = q.encode)
}
(window),
$ = window.$ || {},
$pt = window.$pt || {},
$.Encryption = $pt.Encryption = function() {
    function t(t) {
        return e(t)
    }
    function e(t) {
        return l(n(u(t), t.length * m))
    }
    function n(t, e) {
        t[e >> 5] |= 128 << e % 32,
        t[14 + (e + 64 >>> 9 << 4)] = e;
        for (var n = 1732584193, i = -271733879, c = -1732584194, u = 271733878, l = 0; l < t.length; l += 16) {
            var d = n
              , f = i
              , h = c
              , g = u;
            n = o(n, i, c, u, t[l + 0], 7, -680876936),
            u = o(u, n, i, c, t[l + 1], 12, -389564586),
            c = o(c, u, n, i, t[l + 2], 17, 606105819),
            i = o(i, c, u, n, t[l + 3], 22, -1044525330),
            n = o(n, i, c, u, t[l + 4], 7, -176418897),
            u = o(u, n, i, c, t[l + 5], 12, 1200080426),
            c = o(c, u, n, i, t[l + 6], 17, -1473231341),
            i = o(i, c, u, n, t[l + 7], 22, -45705983),
            n = o(n, i, c, u, t[l + 8], 7, 1770035416),
            u = o(u, n, i, c, t[l + 9], 12, -1958414417),
            c = o(c, u, n, i, t[l + 10], 17, -42063),
            i = o(i, c, u, n, t[l + 11], 22, -1990404162),
            n = o(n, i, c, u, t[l + 12], 7, 1804603682),
            u = o(u, n, i, c, t[l + 13], 12, -40341101),
            c = o(c, u, n, i, t[l + 14], 17, -1502002290),
            i = o(i, c, u, n, t[l + 15], 22, 1236535329),
            n = r(n, i, c, u, t[l + 1], 5, -165796510),
            u = r(u, n, i, c, t[l + 6], 9, -1069501632),
            c = r(c, u, n, i, t[l + 11], 14, 643717713),
            i = r(i, c, u, n, t[l + 0], 20, -373897302),
            n = r(n, i, c, u, t[l + 5], 5, -701558691),
            u = r(u, n, i, c, t[l + 10], 9, 38016083),
            c = r(c, u, n, i, t[l + 15], 14, -660478335),
            i = r(i, c, u, n, t[l + 4], 20, -405537848),
            n = r(n, i, c, u, t[l + 9], 5, 568446438),
            u = r(u, n, i, c, t[l + 14], 9, -1019803690),
            c = r(c, u, n, i, t[l + 3], 14, -187363961),
            i = r(i, c, u, n, t[l + 8], 20, 1163531501),
            n = r(n, i, c, u, t[l + 13], 5, -1444681467),
            u = r(u, n, i, c, t[l + 2], 9, -51403784),
            c = r(c, u, n, i, t[l + 7], 14, 1735328473),
            i = r(i, c, u, n, t[l + 12], 20, -1926607734),
            n = a(n, i, c, u, t[l + 5], 4, -378558),
            u = a(u, n, i, c, t[l + 8], 11, -2022574463),
            c = a(c, u, n, i, t[l + 11], 16, 1839030562),
            i = a(i, c, u, n, t[l + 14], 23, -35309556),
            n = a(n, i, c, u, t[l + 1], 4, -1530992060),
            u = a(u, n, i, c, t[l + 4], 11, 1272893353),
            c = a(c, u, n, i, t[l + 7], 16, -155497632),
            i = a(i, c, u, n, t[l + 10], 23, -1094730640),
            n = a(n, i, c, u, t[l + 13], 4, 681279174),
            u = a(u, n, i, c, t[l + 0], 11, -358537222),
            c = a(c, u, n, i, t[l + 3], 16, -722521979),
            i = a(i, c, u, n, t[l + 6], 23, 76029189),
            n = a(n, i, c, u, t[l + 9], 4, -640364487),
            u = a(u, n, i, c, t[l + 12], 11, -421815835),
            c = a(c, u, n, i, t[l + 15], 16, 530742520),
            i = a(i, c, u, n, t[l + 2], 23, -995338651),
            n = p(n, i, c, u, t[l + 0], 6, -198630844),
            u = p(u, n, i, c, t[l + 7], 10, 1126891415),
            c = p(c, u, n, i, t[l + 14], 15, -1416354905),
            i = p(i, c, u, n, t[l + 5], 21, -57434055),
            n = p(n, i, c, u, t[l + 12], 6, 1700485571),
            u = p(u, n, i, c, t[l + 3], 10, -1894986606),
            c = p(c, u, n, i, t[l + 10], 15, -1051523),
            i = p(i, c, u, n, t[l + 1], 21, -2054922799),
            n = p(n, i, c, u, t[l + 8], 6, 1873313359),
            u = p(u, n, i, c, t[l + 15], 10, -30611744),
            c = p(c, u, n, i, t[l + 6], 15, -1560198380),
            i = p(i, c, u, n, t[l + 13], 21, 1309151649),
            n = p(n, i, c, u, t[l + 4], 6, -145523070),
            u = p(u, n, i, c, t[l + 11], 10, -1120210379),
            c = p(c, u, n, i, t[l + 2], 15, 718787259),
            i = p(i, c, u, n, t[l + 9], 21, -343485551),
            n = s(n, d),
            i = s(i, f),
            c = s(c, h),
            u = s(u, g)
        }
        return 16 == _ ? Array(i, c) : Array(n, i, c, u)
    }
    function i(t, e, n, i, o, r) {
        return s(c(s(s(e, t), s(i, r)), o), n)
    }
    function o(t, e, n, o, r, a, p) {
        return i(e & n | ~e & o, t, e, r, a, p)
    }
    function r(t, e, n, o, r, a, p) {
        return i(e & o | n & ~o, t, e, r, a, p)
    }
    function a(t, e, n, o, r, a, p) {
        return i(e ^ n ^ o, t, e, r, a, p)
    }
    function p(t, e, n, o, r, a, p) {
        return i(n ^ (e | ~o), t, e, r, a, p)
    }
    function s(t, e) {
        var n = (65535 & t) + (65535 & e);
        return (t >> 16) + (e >> 16) + (n >> 16) << 16 | 65535 & n
    }
    function c(t, e) {
        return t << e | t >>> 32 - e
    }
    function u(t) {
        for (var e = Array(), n = (1 << m) - 1, i = 0; i < t.length * m; i += m)
            e[i >> 5] |= (t.charCodeAt(i / m) & n) << i % 32;
        return e
    }
    function l(t) {
        for (var e = w ? "0123456789ABCDEF" : "0123456789abcdef", n = "", i = 0; i < 4 * t.length; i++)
            n += e.charAt(t[i >> 2] >> i % 4 * 8 + 4 & 15) + e.charAt(t[i >> 2] >> i % 4 * 8 & 15);
        return n
    }
    function d(t) {
        for (var e = [], n = 0; n < t.length; n += 2)
            e.push(String.fromCharCode(parseInt(t.substr(n, 2), 16)));
        return e.join("")
    }
    function f(t, e) {
        if (!(Math.random() > (e || 1)))
            try {
                var n = location.protocol + "//ui.ptlogin2.qq.com/cgi-bin/report?id=" + t;
                document.createElement("img").src = n
            } catch (t) {}
    }
    function h(e, n, i, o) {
        i = i || "",
        e = e || "";
        for (var r = o ? e : t(e), a = d(r), p = t(a + n), s = TEA.strToBytes(i.toUpperCase(), !0), c = Number(s.length / 2).toString(16); c.length < 4; )
            c = "0" + c;
        TEA.initkey(p);
        var u = TEA.encrypt(r + TEA.strToBytes(n) + c + s);
        TEA.initkey("");
        for (var l = Number(u.length / 2).toString(16); l.length < 4; )
            l = "0" + l;
        var h = $pt.RSA.rsa_encrypt(d(l + u));
        return setTimeout(function() {
            f(488358, 1)
        }
        , 0),
        btoa(d(h)).replace(/[\/\+=]/g, function(t) {
            return {
                "/": "-",
                "+": "*",
                "=": "_"
            }[t]
        }
        )
    }
    function g(e, n, i) {
        var o = i ? e : t(e)
          , r = o + n.toUpperCase();
        return $.RSA.rsa_encrypt(r)
    }
    var w = 1
      , m = 8
      , _ = 32;
    return {
        getEncryption: h,
        getRSAEncryption: g,
        md5: t
    }
}
();
try {
    window.MTT = function() {
        function t() {
            try {
                if (!o)
                    return !1;
                if (/msie/i.test(navigator.userAgent) && "notify" in window.external)
                    return !0;
                var t = p && o >= 42
                  , e = s && o >= 32
                  , n = a && o >= 42;
                return t || e || n
            } catch (t) {
                return !1
            }
        }
        function e(t) {
            var e = 5 != $.cookie.get("pt_qlogincode") && MTT.canQlogin();
            "function" == typeof t && (e ? /msie/i.test(window.navigator.userAgent) ? window.external.notify("#@getUserInfoWT@#pt.qqBrowserCallback") : a ? browser.login.getLoginInfo ? browser.login.getLoginInfo(t, t) : t("") : (p || s) && (browser.login.getUinAndSidInfo ? browser.login.getUinAndSidInfo(t, t) : t("")) : (t(""),
            5 == $.cookie.get("pt_qlogincode") && $.report.nlog("快速登录异常：pt_qlogincode=5", "276650")))
        }
        function n(t, e) {
            if (!o)
                return e && e(),
                !1;
            if (a) {
                if (!browser.app.getApkInfo)
                    return e && e();
                browser.app.getApkInfo(function(n) {
                    try {
                        n = JSON.parse(JSON.parse(n)),
                        o >= 51 && n && n.versionname >= "4.7" ? t && t() : e && e()
                    } catch (t) {
                        e && e()
                    }
                }
                , "com.tencent.mobileqq")
            } else
                (p || s) && window.x5 && x5.exec(function(n) {
                    n && n.isSupportApp ? t && t() : e && e()
                }
                , e, "app", "getMobileAppSupport", [{
                    scheme: "wtloginmqq2://"
                }]);
            return !1
        }
        function i(t, e) {
            a && browser.login.refreshToken ? browser.login.refreshToken({
                uin: t
            }, e) : (s || p) && window.x5 && x5.exec(e, e, "login", "refreshToken", [{
                uin: t
            }])
        }
        var o = 0
          , r = "";
        try {
            void 0 !== window.browser ? o = browser.env && browser.env.version : window.browser = {
                env: {},
                app: {},
                login: {}
            },
            r = browser.env && browser.env.platForm
        } catch (t) {
            $.report.nlog("browser_env:ver(" + window.ptui_pt_version + ")" + t.message, "647126")
        }
        var a = "ADR" == r
          , p = "I" == r
          , s = "IP" == r;
        return {
            version: o,
            isAndroid: a,
            isIPhone: p,
            canQlogin: t,
            QLogin4PT: e,
            canOneKey: n,
            refreshToken: i
        }
    }
    ()
} catch (t) {
    $.report.nlog("QB Exception:ver(" + window.ptui_pt_version + ")" + t.message, "647127"),
    window.MTT = {}
}
var pt = {
    pageState: 1,
    login_href: "https://ssl.ptlogin2.qq.com",
    domain: window.ptui_domain,
    isHttps: $.check.isHttps(),
    errTipClock: 0,
    lang: window.STR_LANG,
    submit_o: {},
    auto_login: !1,
    switch_position_x: 0,
    touchstartTime: 0,
    longTouchTime: 500,
    default_face_url: "",
    is_qlogin: !1,
    lang_num: window.ptui_lang,
    action: [0, 0],
    vcode: "",
    verifysession: "",
    deviceType: 2,
    login_uin: "",
    login_pwd: "",
    needAt: "",
    appid: "",
    s_url: "",
    low_login_enable: window.ptui_low_login,
    style: 9,
    t_type: 0,
    isSubmiting: !1,
    key_interval: 0,
    keyindex: 19,
    qqBrowserInfo: null ,
    openCookieInfo: null ,
    authInfo: null ,
    authUin: "",
    authNick: "",
    authLoginUrl: "",
    qlogin_list_data: [],
    checkUrl: "",
    loginUrl: "",
    cookieInfo: null ,
    cookieLogin: !1,
    regTmp: '<span id="#uin#" pwd="#pwd#" type="#type#" class="header" onclick="pt.clickHeader(event);">                    <div id="del_touch_#uin#" class="del_touch_icon" >                        <span id="del_#uin#" class="del_icon" ></span>                    </div>                    <img  id="img_#uin#" src="#src#" onerror="pt.face_error();" />                     <div id="img_out_#uin#" class="img_out"></div>                    <label id="nick_#uin#" class="nick">#nick# </label>                </span>',
    hulianRegTmp: '<div class="useravatar">                    <img id="img_#uin#" src="#src#" onerror="pt.face_error();" alt="#nick#" />                  </div>                  <div class="userinfo">                        <div class="usernick" id="hl_usernick">#nick#</div>                        <div class="userqq">#uin#</div>                  </div>                  <button id="userSwitch" class="switch" tabindex="5" href="javascript:void(0)";>切换帐号</button>',
    new_vcode: !1,
    clickEvent: "touchstart",
    checkErr: {
        2052: "网络繁忙，请稍后重试。",
        1028: "網絡繁忙，請稍後重試。",
        1033: "The network is busy, please try again later."
    },
    isHulian: 716027609 == window.ptui_appid,
    isOffice: 39 == window.ptui_style,
    isWtlogin: 42 == window.ptui_style,
    isInIframe: 38 == window.ptui_style,
    is3gNews: 37 == window.ptui_style,
    isMail: 522005705 == window.ptui_appid,
    lockedAccount: 1 == window.ptui_lockuin ? window.ptui_defuin : "",
    ua: navigator.userAgent.toLowerCase(),
    isWX: navigator.userAgent.match(/micromessenger\/(\d\.\d\.\d)/i),
    isMQQ: navigator.userAgent.match(/qq\/(\d\.\d\.\d)/i),
    isAndroid: /android/i.test(navigator.userAgent),
    isIos: /iphone|ipad/i.test(navigator.userAgent),
    isIPhone: /iphone/i.test(navigator.userAgent),
    uInput: $("u"),
    pInput: $("p"),
    btnGo: $("go"),
    btnOnekey: $("onekey"),
    redirect: function(t, e) {
        switch (t + "") {
        case "0":
            pt.isInIframe || "1" == $.bom.query("pt_replace") ? location.replace(e) : location.href = e;
            break;
        default:
            "1" == $.bom.query("pt_replace") ? top.location.replace(e) : top.location.href = e
        }
    },
    init: function() {
        pt.hasInit || (pt.hasInit = !0,
        pt.default_face_url = "//imgcache.qq.com/ptlogin/v4/style/0/images/1.gif",
        pt.initSURL(),
        pt.setClickEvent(),
        pt.isOffice && pt.open.loadAppInfo(),
        pt.qqBrowserQlogin(),
        pt.auth(),
        pt.bindEvent(),
        pt.bindInput(),
        pt.hideURLBar(),
        pt.setVcodeFlag(),
        pt.setUrl(),
        pt.showAutoLogin(),
        $.winName.set("login_href", encodeURIComponent(pt.login_href)),
        pt.checkIframe(),
        pt.checkPostMessage(),
        42 == window.ptui_style && pt.uInput && pt.uInput.focus(),
        window.setTimeout(function(t) {
            "549000929" != window.ptui_appid && pt.webLoginReport(),
            $.report.monitor(412020, .05),
            navigator.cookieEnabled || ($.report.monitor(410030),
            $.cookie.get("ptcz") && $.report.monitor(410031))
        }
        , 2e3),
        $.http.loadScript((pt.isHttps ? "https://ssl.captcha.qq.com/" : "http://captcha.qq.com/") + "template/TCapIframeApi.js?aid=" + window.ptui_appid + "&rand=" + Math.random() + "&clientype=1&lang=" + pt.lang_num + "&apptype=2", function() {}
        ))
    },
    VCCallback: function(t) {
        switch (+t.ret) {
        case 0:
            pt.submitNewVcode(t);
            break;
        case 1:
            pt.go_back()
        }
    },
    setUrl: function() {
        var t = pt.isHttps ? "https://ssl." : "http://"
          , e = pt.isHttps ? "https://ssl." : "http://check.";
        pt.checkUrl = e + "ptlogin2." + pt.domain + "/check?",
        pt.loginUrl = t + "ptlogin2." + pt.domain + "/"
    },
    ptui_speedReport: function(t) {
        var e = "http://isdspeed.qq.com/cgi-bin/r.cgi?flag1=7808&flag2=8"
          , n = 1;
        if (pt.isHttps)
            e = "https://huatuospeed.weiyun.com/cgi-bin/r.cgi?flag1=7808&flag2=8",
            n = 2;
        else if ("MQQBrowser" == $.detectBrowser()[0]) {
            var i = navigator.connection;
            if (i && i.type) {
                var o = i.type;
                n = 1 == o ? 3 : 2 == o ? 4 : 3 == o ? 5 : 4 == o ? 6 : 5 == o ? 7 : 8
            } else
                n = 8
        } else
            n = 1;
        e += "&flag3=" + n;
        for (var r in t)
            t[r] > 15e3 || t[r] < 0 || (e += "&" + r + "=" + (t[r] || 1));
        (new Image).src = e
    },
    webLoginReport: function() {
        try {
            if (Math.random() > .2 && "MQQBrowser" != $.detectBrowser()[0])
                return;
            var t = ["navigationStart", "unloadEventStart", "unloadEventEnd", "redirectStart", "redirectEnd", "fetchStart", "domainLookupStart", "domainLookupEnd", "connectStart", "connectEnd", "requestStart", "responseStart", "responseEnd", "domLoading", "domInteractive", "domContentLoadedEventStart", "domContentLoadedEventEnd", "domComplete", "loadEventStart", "loadEventEnd"]
              , e = {}
              , n = window.performance ? window.performance.timing : null ;
            if (n) {
                for (var i = n[t[0]], o = 1, r = t.length; o < r; o++)
                    n[t[o]] && (e[o] = n[t[o]] - i);
                loadJs && loadJs.onloadTime && (e[o++] = loadJs.onloadTime - i);
                n.connectEnd >= n.connectStart && n.responseEnd >= n.responseStart && n.domComplete >= n.domInteractive && n.domInteractive >= n.domLoading && n.loadEventStart >= n.domComplete && n.loadEventEnd >= n.loadEventStart && pt.ptui_speedReport(e)
            }
        } catch (t) {}
    },
    setClickEvent: function() {
        /iphone|ipad|android/.test(navigator.userAgent.toLowerCase());
        pt.clickEvent = "click"
    },
    saveLastUin: function(t) {
        $.localStorage.set("last_uin", t)
    },
    getLastUin: function() {
        return $.localStorage.get("last_uin")
    },
    object2param: function(t) {
        var e = [];
        for (var n in t)
            e.push(n + "=" + t[n] + "&");
        return e.join("")
    },
    alertBackground: null ,
    alertDiv: null ,
    showAlert: function(t) {
        pt.alertBackground || (pt.alertBackground = document.createElement("div"),
        pt.alertBackground.className = "qui-dialog-mask"),
        pt.alertDiv || (pt.alertDiv = document.createElement("div"),
        pt.alertDiv.className = "qui-dialog-box"),
        pt.alertDiv.innerHTML = '<div class="qui-dialog-content">' + t + '</div><div class="qui-dialog-bottom" onclick="pt.hideAlert()">' + STR_LANG.close + "</div>",
        document.body.appendChild(pt.alertBackground),
        document.body.appendChild(pt.alertDiv)
    },
    hideAlert: function() {
        document.body.removeChild(pt.alertBackground),
        document.body.removeChild(pt.alertDiv)
    },
    is_oversea: !1,
    enterOverseaLogin: function() {
        try {
            pt.hideAlert()
        } catch (t) {}
        var t = $("oversea");
        t && $.css.show(t),
        pt.is_oversea = !0,
        $("state").value = _areaList[0].n,
        $("country-code").innerHTML = "+" + _areaList[0].c,
        window.openSDK && window.openSDK.clearAllEdit && ($("password").value = "",
        window.openSDK.clearAllEdit())
    },
    exitOverseaLogin: function() {
        var t = $("oversea");
        t && $.css.hide(t),
        pt.is_oversea = !1,
        window.openSDK && window.openSDK.clearAllEdit && (pt.pInput.value = "",
        window.openSDK.clearAllEdit())
    },
    enterOverseaCountry: function() {
        pt.initOverseaCountry();
        var t = $("country");
        t && $.css.show(t);
        var e = $("state");
        e && e.blur()
    },
    setOverseaCountry: function(t) {
        for (var e in _areaList)
            if (t == _areaList[e].n)
                return $("state").value = _areaList[e].n,
                $("country-code").innerHTML = "+" + _areaList[e].c,
                void pt.exitOverseaCountry()
    },
    initOverseaCountry: function() {
        var t = $("country-search")
          , e = $("country-list")
          , n = "";
        if (t.value = "",
        _areaList)
            for (var i in _areaList)
                n += "<li onclick=\"pt.setOverseaCountry('" + _areaList[i].n + "')\">" + _areaList[i].n + "</li>";
        e.innerHTML = n
    },
    updateOverseaCountry: function() {
        var t = $("country-search")
          , e = $("country-list")
          , n = "";
        if (_areaList) {
            var i = new RegExp(t.value.split("").join(".{0,}"),"i");
            for (var o in _areaList)
                (i.test(_areaList[o].c) || i.test(_areaList[o].n) || i.test(_areaList[o].p)) && (n += "<li onclick=\"pt.setOverseaCountry('" + _areaList[o].n + "')\">" + _areaList[o].n + "</li>")
        }
        e.innerHTML = n
    },
    countrySearchFocus: function() {
        var t = $("country-search-placeholder");
        t && $.css.hide(t)
    },
    countrySearchBlur: function() {
        var t = $("country-search-placeholder")
          , e = $("country-search");
        e && t && 0 === e.value.length && $.css.show(t)
    },
    exitOverseaCountry: function() {
        var t = $("country");
        t && $.css.hide(t)
    },
    showErr: function(t, e) {
        clearTimeout(pt.errTipClock);
        var n = 3e3;
        "number" == (typeof e).toLocaleLowerCase() && (n = parseInt(e, 10),
        e = null ),
        $("error_message").innerHTML = t,
        $.css.show("error_tips"),
        pt.isHulian ? (navigator.userAgent.match(/iphone/i) && pt.btnGo.focus(),
        pt.errTipClock = setTimeout(function() {
            pt.hideErr(e)
        }
        , n)) : (e && e(),
        pt.errTipClock = setTimeout(function() {
            pt.hideErr()
        }
        , n))
    },
    hideErr: function(t) {
        $.css.hide("error_tips"),
        t && t()
    },
    checkIframe: function() {
        try {
            top == self || pt.isHulian || $.report.nlog("iphone登录框被iframe;referer=" + document.referrer, "347748")
        } catch (t) {}
    },
    checkPostMessage: function() {
        void 0 === window.postMessage && $.report.nlog("iphone登录框不支持postMessage;", "350525"),
        void 0 === window.JSON && $.report.nlog("iphone登录框不支持JSON;", "362678")
    },
    setVcodeFlag: function() {
        void 0 === window.postMessage || void 0 === window.JSON ? pt.new_vcode = !1 : pt.new_vcode = !0
    },
    getAuthUrl: function() {
        var t = (pt.isHttps ? "https://ssl." : "http://") + "ptlogin2." + pt.domain + "/pt4_auth?daid=" + window.ptui_daid + "&appid=" + window.ptui_appid + "&auth_token=" + $.str.time33($.cookie.get("supertoken"));
        return "1" == window.ptui_pt_qzone_sig && (t += "&pt_qzone_sig=1"),
        t
    },
    auth: function() {
        pt.getParam(),
        pt.initSURL();
        var t = pt.getAuthUrl()
          , e = $.cookie.get("superuin")
          , n = $.str.hash33(e);
        !parseInt($.cookie.get("supertoken")) && n && $.cookie.set("supertoken", $.str.hash33(e), "ptlogin2." + pt.domain),
        window.ptui_daid && "1" != window.ptui_noAuth && "" != e && !pt.isWtlogin && $.http.loadScript(t)
    },
    showAuth: function(t) {
        var e = t.substr(t.indexOf("?") + 1)
          , n = e.match(RegExp("(^|&)uin=([^&]*)(&|$)"));
        pt.authUin = n ? decodeURIComponent(n[2]) : "",
        pt.authLoginUrl = t,
        pt.authNick = $.str.utf8ToUincode($.cookie.get("ptnick_" + pt.authUin)) || pt.authUin,
        pt.authUin && (pt.authInfo = {
            uin: $.str.encodeHtml(pt.authUin),
            nick: $.str.encodeHtml(pt.authNick),
            authUrl: pt.authUrl,
            type: 3
        })
    },
    setCookieLogin: function() {
        var t = $.cookie.get("skey")
          , e = $.cookie.get("supertoken")
          , n = $.cookie.uin()
          , i = $.str.utf8ToUincode($.cookie.get("ptnick_" + n)) || n;
        return !(!(window.pt_skey_valid && t || window.ptui_daid && e) || !n || (pt.cookieInfo = {
            uin: $.str.encodeHtml(n),
            nick: $.str.encodeHtml(i),
            superkey: e,
            skey: t,
            type: 4
        },
        0))
    },
    qqBrowserQlogin: function() {
        try {
            (self === top || MTT.isAndroid) && MTT.QLogin4PT(pt.qqBrowserCallback)
        } catch (t) {
            $.report.nlog("快速登录异常,qqBrowserQlogin," + t.message, "276650")
        }
    },
    qqBrowserCallback: function(t) {
        try {
            t && "string" == typeof t && (t = JSON.parse(t)),
            t && $.check.isQQ(t.uin) && 0 != t.loginkey.length && t.loginkey.length > 10 ? (pt.qqBrowserInfo = {},
            pt.qqBrowserInfo.uin = $.str.encodeHtml(t.uin),
            pt.qqBrowserInfo.nick = $.str.encodeHtml(t.nickname),
            pt.qqBrowserInfo.loginkey = t.loginkey,
            pt.qqBrowserInfo.type = 2,
            pt.refreshQloginUI()) : t && 0 == t.uin.length ? $.report.nlog("快速登录异常：数据返回异常,没有uin", "276650") : t && 0 == t.loginkey.length ? $.report.nlog("快速登录异常：数据返回异常,没有loginkey", "276650") : t && $.report.nlog("快速登录异常：数据返回异常:" + t.loginkey.length, "276650")
        } catch (t) {
            $.report.nlog("快速登录异常： qqBrowserCallback " + t.message, "276650")
        }
    },
    initSURL: function() {
        pt.s_url = $.bom.query("s_url"),
        pt.isMail && 1 == pt.low_login_enable && (pt.s_url = pt.addParamToUrl(pt.s_url, "ss", 1))
    },
    addParamToUrl: function(t, e, n) {
        var i = t.split("#")
          , o = i[0].indexOf("?") > 0 ? "&" : "?";
        return "?" == i[0].substr(i[0].length - 1, 1) && (o = ""),
        i[1] ? i[1] = "#" + i[1] : i[1] = "",
        i[0] + o + e + "=" + n + i[1]
    },
    getParam: function() {
        if (pt.appid = window.ptui_appid,
        pt.isInIframe)
            switch (window.ptui_target) {
            case "_self":
                pt.target = 0;
                break;
            case "_top":
            default:
                pt.target = 1
            }
        else
            pt.target = 1;
        pt.style = window.ptui_style ? window.ptui_style : 9,
        pt.isHulian && (window.pt_skey_valid = parseInt($.bom.query("pt_skey_valid")) || 0)
    },
    build_qlogin_list: function() {
        var t = pt.get_qlogin_list();
        pt.qlogin_list_data = t;
        var e = t.length;
        if (e > 0 && !window.ptui_tab) {
            pt._switch(2),
            pt.hideOneKey();
            for (var n = "", i = 0; i < e; i++)
                "" != t[i].uin && (n += pt.regTmp.replace(/#uin#/g, t[i].uin).replace(/#nick#/g, t[i].nick).replace(/#pwd#/g, t[i].pwd).replace(/#type#/g, t[i].type).replace(/#src#/g, pt.default_face_url));
            e > 1 && xMsg && xMsg.call && xMsg.call("connect", "userSwitch", {}, function() {}
            ),
            $("q_logon_list").innerHTML = n;
            for (var i = 0; i < e; i++)
                pt.getShortWord($("nick_" + t[i].uin), t[i].nick, 95);
            pt.initFace(),
            $("swicth_login") && ($("swicth_login").style.display = "block")
        } else
            $("web_login") && ($("web_login").style.display = "block"),
            $("swicth_login") && ($("swicth_login").style.display = "none")
    },
    fill_usernick: function() {
        window.mqq && mqq.data && mqq.data.getUserInfo && mqq.data.getUserInfo(function(t) {
            var e = $("hl_usernick");
            e && t && t.nick && (e.innerHTML = $.str.encodeHtml(t.nick))
        }
        )
    },
    build_office_qlogin: function() {
        if ($.report.monitor(2123219),
        pt.cookieInfo || pt.setCookieLogin(),
        !pt.cookieInfo)
            return $.report.monitor(2123220);
        pt.cookieLogin = !0,
        $("hl_avatar").style.backgroundImage = "url(https://q4.qlogo.cn/g?b=qq&nk=" + pt.cookieInfo.uin + "&s=100)",
        $("hl_usernick").innerHTML = pt.cookieInfo.nick || pt.cookieInfo.uin,
        $("hl_qqnum").innerHTML = "(" + pt.cookieInfo.uin + ")",
        pt.fill_usernick()
    },
    build_hulian_qlogin_list: function() {
        var t = pt.openCookieInfo || {};
        t.nick && pt.cookieInfo && (pt.cookieInfo.nick = $.str.encodeHtml(t.nick));
        var e = pt.get_qlogin_list();
        pt.qlogin_list_data = e;
        var n = window.ptui_daid && 1 == t.superkey || t.skey
          , i = e.length;
        if (i > 0) {
            pt.hideOneKey();
            for (var o = "", r = 0; r < i; r++) {
                var a = e[r];
                if (n || !pt.cookieInfo || pt.cookieInfo.uin != a.uin || r == i - 1) {
                    "" != a.uin && (o += pt.hulianRegTmp.replace(/#uin#/g, a.uin).replace(/#nick#/g, a.nick).replace(/#type#/g, a.type).replace(/#src#/g, pt.default_face_url));
                    break
                }
            }
            setTimeout(function() {
                var t = $("q_logon_list");
                t.innerHTML = o,
                $.css.show(t),
                $.css.hide($("form_outter_wrap")),
                pt.cookieLogin = n,
                pt.fill_usernick();
                var e = $("userSwitch");
                window.ptui_lockuin ? e && $.css.hide(e) : e && $.e.add(e, "click", function(t) {
                    pt.showOneKey.ever && pt.showOneKey(),
                    pt.qqBrowserInfo = null ,
                    pt.open.authListDone && !window.ptui_tab && (xMsg.call("connect", "userSwitch", {}, function() {}
                    ),
                    pt.cancel_cookielogin(!0),
                    $.report.monitor(2106346))
                }
                )
            }
            , 0)
        }
    },
    _switch: function(t) {
        var e = function() {
            $("q_login").style.display = "block",
            $("web_login").style.display = "none",
            $("swicth_login") && ($("swicth_login").innerHTML = $.str.encodeHtml(qlogin_wording)),
            pt.hideURLBar(),
            pt.pageState = 2
        }
          , n = function() {
            pt.showOneKey.ever && pt.showOneKey(),
            $("q_login").style.display = "none",
            $("web_login").style.display = "block",
            $("swicth_login") && ($("swicth_login").innerHTML = $.str.encodeHtml(login_wording)),
            pt.uInput.focus(),
            pt.pageState = 1,
            xMsg.call("connect", "userSwitch", {}, function() {}
            )
        }
        ;
        if (void 0 === t)
            "none" == $("q_login").style.display ? e() : n();
        else
            switch (+t) {
            case 2:
                e();
                break;
            case 1:
                n()
            }
        pt.showAutoLogin(),
        pt.isInIframe && window.setTimeout(function() {
            pt.ptui_notifySize("content")
        }
        , 0)
    },
    checkNetwork: function() {
        if (navigator.onLine)
            return pt._timer = setTimeout(function() {
                $.report.monitor(2114669),
                pt.showErr(STR_LANG.offline);
                try {
                    pt.endLoading()
                } catch (t) {}
            }
            , 1e4);
        pt.showErr(STR_LANG.offline)
    },
    loadingDiv: null ,
    isLoading: !1,
    startLoading: function() {
        pt.loadingDiv || (pt.loadingDiv = document.createElement("div"),
        pt.loadingDiv.className = "qui-loading-mask"),
        document.body.appendChild(pt.loadingDiv),
        pt.isloading = !0
    },
    endLoading: function() {
        document.body.removeChild(pt.loadingDiv),
        pt.isloading = !1
    },
    submitEvent: function(t) {
        pt.checkNetwork(),
        pt.startLoading(),
        pt.isHulian && pt.cookieLogin ? pt.cookielogin_submit() : pt.isHulian && pt.qqBrowserInfo ? pt.qlogin_submit() : window.openSDK && openSDK.getMD5FromNative ? openSDK.getMD5FromNative(function() {
            pt.check(!1)
        }
        ) : pt.check(!1),
        pt.qrcode.used = !1
    },
    showOneKey: function(t) {
        var e = $("onekey");
        if (pt.showOneKey.ever || ($.e.add(e, pt.clickEvent, pt.doOneKey),
        $.e.add(e, "blur", pt.cancelAutoOneKey)),
        pt.showOneKey.ever = !0,
        t ? pt.btnGo.className += " weak" : e.className += " weak",
        e && $.css.show(e),
        e.focus(),
        $.report.monitor(414089),
        pt.isInIframe && window.setTimeout(function() {
            pt.ptui_notifySize("content")
        }
        , 0),
        t && "justshow" != t) {
            var n = e.innerHTML
              , i = 3
              , o = n + "中({{second}}秒...)";
            e.innerHTML = o.replace("{{second}}", i--),
            pt.showOneKey.tid = setInterval(function() {
                e.innerHTML = o.replace("{{second}}", i--),
                i < 0 && (i = 3,
                clearInterval(pt.showOneKey.tid),
                e.innerHTML = n,
                pt.doOneKey())
            }
            , 1e3)
        }
    },
    cancelAutoOneKey: function() {
        clearInterval(pt.showOneKey.tid);
        var t = pt.btnOnekey;
        t && (t.innerHTML = STR_LANG.onekey)
    },
    hideOneKey: function() {
        pt.cancelAutoOneKey(),
        pt.btnGo.className = pt.btnGo.className.replace("weak", ""),
        pt.btnOnekey && $.css.hide(pt.btnOnekey),
        pt.isInIframe && window.setTimeout(function() {
            pt.ptui_notifySize("content")
        }
        , 0)
    },
    bindEvent: function() {
        var t = pt.uInput
          , e = pt.pInput;
        $.e.add(pt.btnGo, pt.clickEvent, pt.submitEvent),
        e && $.e.add(e, "keydown", function(t) {
            13 == t.keyCode && pt.submitEvent()
        }
        ),
        t && $.e.add(t, "keydown", function(t) {
            13 == t.keyCode && (pt.checkNetwork(),
            pt.startLoading(),
            pt.check(!1))
        }
        );
        var n = navigator.userAgent.toLowerCase()
          , i = pt.isWX || pt.isMQQ || n.match(/meizu_m9|IEMobile/i) || 46000101 == window.ptui_appid || pt.s_url.indexOf("//openmobile.qq.com/api/check") >= 0
          , o = pt.btnOnekey;
        if (!i && o)
            if (pt.isHulian)
                n.match(/iphone|ipad/i) && (document.addEventListener("touchmove", function() {
                    pt.btnGo.focus()
                }
                , !1),
                document.addEventListener("touchstart", function(t) {
                    pt.uInput == t.target && pt.pInput == t.target || pt.btnGo.focus()
                }
                , !1)),
                pt.open.waiting("authlist", function() {
                    MTT.canOneKey(function() {
                        pt.get_qlogin_list().length || pt.showOneKey(!0)
                    }
                    , function() {
                        pt.hideOneKey()
                    }
                    )
                }
                );
            else if (self === top && MTT.version)
                MTT.canOneKey(function() {
                    pt.get_qlogin_list().length || pt.showOneKey("justshow")
                }
                , function() {
                    pt.hideOneKey()
                }
                );
            else {
                var n = navigator.userAgent
                  , r = n.indexOf("Windows NT") > -1 || n.indexOf("Macintosh") > -1;
                r || pt.isWtlogin || pt.showOneKey()
            }
        else
            pt.hideOneKey();
        if ($("show_pwd") && $.e.add($("show_pwd"), "change", function(t) {
            var e = pt.pInput;
            this.checked ? e.setAttribute("type", "text") : e.setAttribute("type", "password")
        }
        ),
        $("forgetpwd") && $.e.add($("forgetpwd"), pt.clickEvent, function(t) {
            var e = pt.uInput && pt.uInput.value
              , n = "https://ssl.ptlogin2.qq.com/ptui_forgetpwd_mobile?ptlang=" + pt.lang_num;
            "1033" != pt.lang_num && (n += "&account=" + e),
            window.open(n)
        }
        ),
        $.e.add(window, "orientationchange", function(t) {
            pt.hideURLBar(t)
        }
        ),
        pt.isMail) {
            var a = $("remember");
            if (!a)
                return;
            $.e.add(a, "change", function() {
                a.checked ? pt.s_url = pt.addParamToUrl(pt.s_url, "ss", 1) : pt.s_url = pt.s_url.replace(/&?ss=1/, ""),
                pt.low_login_enable = a.checked ? 1 : 0
            }
            )
        }
        if ($("download-link") && $("download-area")) {
            $.e.add($("download-area"), "click", function(t) {
                return t.target !== $("download-link") && $("download-link").click(),
                !1
            }
            );
            var p = [[/android/i, "market://details?id=com.tencent.mobileqq"], [/ipad|iphone/i, "itms-apps://itunes.apple.com/us/app/qq/id444934666?mt=8"]]
              , s = [[/android/i, "market://details?id=com.tencent.mobileqqi"], [/ipad|iphone/i, "itms-apps://itunes.apple.com/us/app/qq-international/id710380093?mt=8"]];
            switch (parseInt(ptui_lang)) {
            case 1033:
            case 1028:
                for (var c in p)
                    if (s[c][0].test(navigator.userAgent)) {
                        $("download-link").href = s[c][1];
                        break
                    }
                0 == $("download-link").href.length && ($("download-link").href = "http://www.imqq.com/?lang=" + ptui_lang);
                break;
            default:
                for (var c in p)
                    if (p[c][0].test(navigator.userAgent)) {
                        $("download-link").href = p[c][1];
                        break
                    }
                0 == $("download-link").href.length && ($("download-link").href = "https://im.qq.com")
            }
            var n = navigator.userAgent;
            /ipad/i.test(n) ? $("download-link-pad").href = "itms-apps://itunes.apple.com/cn/app/qq-hd-2011/id453718989?mt=8" : /android/i.test(n) && !/mobile/i.test(n) ? $("download-link-pad").href = "market://details?id=com.tencent.minihd.qq" : $("download-link-pad").href = $("download-link").href
        }
        $("qrlogin_switch") && $.e.add($("qrlogin_switch"), "click", function() {
            return pt.switchpwd(),
            !1
        }
        ),
        $("qr_invalid") && $.e.add($("qr_invalid"), "click", function() {
            pt.qrcode.get(0),
            $.css.hide($("qr_invalid"))
        }
        ),
        document.body.addEventListener("focus", function(t) {
            t.target.scrollIntoViewIfNeeded ? t.target.scrollIntoViewIfNeeded() : t.target.scrollIntoView && t.target.scrollIntoView()
        }
        , !0),
        $("switcher_qlogin") && $.e.add($("switcher_qlogin"), "click", function() {
            return pt.switchqr(),
            !1
        }
        ),
        setTimeout(function() {
            window.ptui_tab && (756044602 != window.ptui_appid ? pt.qrcode.get(0) : pt.switchpwd())
        }
        , 0)
    },
    bindInput: function() {
        if (!pt.isOffice) {
            var t = window.ptui_defuin || pt.lockedAccount || pt.getLastUin()
              , e = pt.uInput
              , n = pt.pInput
              , i = $("del_touch") || $("del_u")
              , o = $("del_touch_p") || $("del_p")
              , r = $("phone")
              , a = $("password")
              , p = $("del_phone")
              , s = $("del_password")
              , c = function(t, e, n, i) {
                return function(o) {
                    e ? t && $.css.show(t) : t && $.css.hide(t),
                    i && n && (n.style.paddingRight = e ? "20px" : "")
                }
            }
              , u = c(i, !0, i.parentNode, 42 == ptui_style)
              , l = c(i, !1, i.parentNode, 42 == ptui_style)
              , d = c(o, !0, o.parentNode, 42 == ptui_style)
              , f = c(o, !1, o.parentNode, 42 == ptui_style)
              , h = c(p, !0, p.parentNode, !0)
              , g = c(p, !1, p.parentNode, !0)
              , w = c(s, !0, s.parentNode, !0)
              , m = c(s, !1, s.parentNode, !0);
            t && (e.value = "0" == t ? "" : e.value || t),
            pt.lockedAccount && (e.readOnly = !0,
            n.focus());
            var _ = function(t, e, n) {
                return function(i) {
                    if (!pt.lockedAccount || n) {
                        var o = i.target;
                        if ("" != i.target.value ? t() : e(),
                        n) {
                            var r = 0;
                            (o.selectionStart || "0" == o.selectionStart) && (r = Math.max(o.selectionStart, o.selectionEnd)),
                            window.openSDK && window.openSDK.curPosFromJS && window.openSDK.curPosFromJS(r)
                        }
                    }
                }
            }
              , v = function(t, e) {
                return function(n) {
                    "" != n.target.value && t(),
                    e && window.openSDK && window.openSDK.isPasswordEdit && window.openSDK.isPasswordEdit(1)
                }
            }
              , y = function(t, e, n) {
                return function(i) {
                    "" == i.target.value && t(),
                    e ? window.openSDK && window.openSDK.isPasswordEdit && window.openSDK.isPasswordEdit(0) : (/^\+/.test(i.target.value) && (i.target.value = i.target.value.replace(/^\+/, ""),
                    /^00/.test(i.target.value) || (i.target.value = "00" + i.target.value)),
                    "" == i.target.value ? t() : n && pt.checkQQUin(i.target.value))
                }
            }
            ;
            $.e.add(n, "focus", v(d, !0)),
            $.e.add(n, "blur", y(f, !0)),
            $.e.add(n, "input", function(t) {
                window.setTimeout(function() {
                    _(d, f, !0)(t)
                }
                , 0)
            }
            ),
            $.e.add(e, "focus", v(u)),
            $.e.add(e, "blur", y(l, !1, !0)),
            $.e.add(e, "input", _(u, l)),
            $.e.add(a, "focus", v(w, !0)),
            $.e.add(a, "blur", y(m, !0)),
            $.e.add(a, "input", function(t) {
                window.setTimeout(function() {
                    _(w, m, !0)(t)
                }
                , 0)
            }
            ),
            $.e.add(r, "focus", v(h)),
            $.e.add(r, "blur", y(g, !1, !1)),
            $.e.add(r, "input", _(h, g));
            var b = function(t, e, n) {
                return function(i) {
                    i && i.preventDefault(),
                    t.value = "",
                    n && window.openSDK && window.openSDK.clearAllEdit && window.openSDK.clearAllEdit(),
                    t.focus(),
                    e()
                }
            }
            ;
            i && $.e.add(i, "click", b(e, l)),
            o && $.e.add(o, "click", b(n, f, !0)),
            p && $.e.add(p, "click", b(r, g)),
            s && $.e.add(s, "click", b(a, m))
        }
    },
    bindVcodeEvent: function() {
        $("input_tips") && $.e.add($("input_tips"), "click", function(t) {
            $("vcode_input").focus(),
            $.css.hide("input_tips"),
            t.stopPropagation()
        }
        ),
        $("vcode_input") && $.e.add($("vcode_input"), "focus", function(t) {
            $.css.hide("input_tips"),
            t.stopPropagation()
        }
        ),
        $("vcode_input") && $.e.add($("vcode_input"), "blur", function(t) {
            "" == this.value && $.css.show("input_tips")
        }
        ),
        $("vcode_img") && $.e.add($("vcode_img"), "click", function(t) {
            $("vcode_input").focus(),
            $.css.hide("input_tips"),
            pt.changeCodeImg(),
            t.stopPropagation()
        }
        ),
        $("submit") && $.e.add($("submit"), "click", function(t) {
            pt.submitVcode()
        }
        )
    },
    hideURLBar: function() {
        setTimeout(function() {
            window.scrollTo(0, 1)
        }
        , 0)
    },
    showAutoLogin: function() {
        if (pt.isMail) {
            var t = $("auto_login");
            if (t) {
                var e = pt.btnGo;
                if (1 == pt.pageState)
                    $("web_login").insertBefore(t, e);
                else {
                    var n = $("q_login");
                    n.insertBefore(t, n.lastChild)
                }
                $.css.show(t)
            }
        }
    },
    doOneKey: function(t) {
        if (!pt.doOneKey.ing) {
            pt.doOneKey.ing = !0,
            setTimeout(function() {
                pt.doOneKey.ing = !1
            }
            , 5e3);
            var e = navigator.userAgent.toLowerCase()
              , n = pt.loginUrl + "jump?u1=" + encodeURIComponent(pt.s_url) + "&pt_report=1";
            "1" == window.ptui_pt_ttype && (n += "&pt_ttype=1"),
            window.ptui_daid && (n += "&daid=" + ptui_daid),
            pt.low_login_enable && (n += "&low_login_enable=1&low_login_hour=" + window.ptui_low_login_hour),
            n += "&style=" + window.ptui_style;
            var i = $.detectBrowser()[0];
            i && (n += "&pt_ua=" + $.Encryption.md5(e),
            n += "&pt_browser=" + i);
            var o = $.bom.query("pt_appname");
            o && (n += "&pt_appname=" + o);
            var r = $.bom.query("pt_package");
            if (/android/i.test(navigator.userAgent))
                r && (n += "&pt_package=" + r);
            else {
                var a = $.bom.query("pt_bundleid") || r;
                a && (n += "&pt_bundleid=" + a)
            }
            $.report.monitor(414090),
            pt.isHulian ? pt.open.waiting("authdata", function() {
                window.ptui_pt_3rd_aid && (n += "&pt_3rd_aid=" + window.ptui_pt_3rd_aid),
                pt.submit_o.openlogin_data && (n += "&pt_openlogin_data=" + pt.submit_o.openlogin_data),
                OneKey("wtloginmqq://ptlogin/qlogin?p=" + encodeURIComponent(n))
            }
            ) : OneKey("wtloginmqq://ptlogin/qlogin?p=" + encodeURIComponent(n))
        }
    },
    addToSet: function(t, e) {
        if (e) {
            for (var n = e.uin, i = !0, o = 0, r = t.length; o < r; o++)
                t[o].uin == n && (i = !1);
            i && t.push(e)
        } else
            ;
    },
    get_qlogin_list: function() {
        var t = [];
        return pt.isHulian ? pt.cookieInfo && pt.addToSet(t, pt.cookieInfo) : pt.authInfo && pt.addToSet(t, pt.authInfo),
        pt.qqBrowserInfo && pt.addToSet(t, pt.qqBrowserInfo),
        t
    },
    qlogin_submit: function(t) {
        $.report.monitor(259519);
        var e, n = encodeURIComponent(pt.s_url);
        if (t == pt.qrcode.CGI)
            e = pt.loginUrl + t + "?u1=" + n + "&daid=" + window.ptui_daid,
            e += "&from_ui=1&type=1&ptlang=" + pt.lang_num,
            e += "&ptqrtoken=" + $.str.hash33($.cookie.get("qrsig"));
        else {
            var i = pt.qqBrowserInfo.uin
              , o = pt.qqBrowserInfo.loginkey;
            e = pt.loginUrl + "jump?keyindex=" + pt.keyindex + "&clientuin=" + i + "&clientkey=" + o + "&u1=" + n + "&daid=" + window.ptui_daid
        }
        if (window.ptui_appid && (e += "&aid=" + window.ptui_appid),
        "1" == window.ptui_pt_qzone_sig && (e += "&pt_qzone_sig=1"),
        "1" == window.ptui_pt_ttype && (e += "&pt_ttype=1"),
        "1" == window.ptui_pt_light && (e += "&pt_light=1"),
        pt.low_login_enable && (e += "&low_login_enable=1&low_login_hour=" + window.ptui_low_login_hour),
        window.ptui_pt_3rd_aid && (e += "&pt_3rd_aid=" + window.ptui_pt_3rd_aid),
        pt.submit_o.openlogin_data && (e += "&pt_openlogin_data=" + pt.submit_o.openlogin_data),
        "0" != window.ptui_kf_csimc && window.ptui_kf_csimc && (e += "&csimc=" + ptui_kf_csimc,
        e += "&csnum=" + ptui_kf_csnum,
        e += "&authid=" + ptui_kf_authid),
        e += "&device=" + pt.deviceType,
        e += "&ptopt=1",
        e += "&pt_uistyle=" + window.ptui_style,
        t == pt.qrcode.CGI)
            return e;
        $.http.loadScript(e)
    },
    cookielogin_submit: function() {
        var t = pt.cookieInfo.skey
          , e = t && $.str.hash33(t);
        pt.submit_o.skey_token = e,
        pt.submit("open")
    },
    cancel_cookielogin: function(t) {
        try {
            $.css.show($("form_outter_wrap")),
            $.css.hide($("q_logon_list"))
        } catch (t) {}
        pt.cookieLogin = !1,
        delete pt.submit_o.skey_token,
        pt.cookieInfo = null ,
        t && (pt.uInput.value = "")
    },
    authlogin_submit: function() {
        var t = pt.authLoginUrl;
        t += "&regmaster=" + window.ptui_regmaster + "&aid=" + window.ptui_appid + "&s_url=" + encodeURIComponent(pt.s_url),
        pt.low_login_enable && (t += "&low_login_enable=1&low_login_hour=" + window.ptui_low_login_hour),
        "1" == window.ptui_pt_ttype && (t += "&pt_ttype=1"),
        "1" == window.ptui_pt_light && (t += "&pt_light=1"),
        t += "&device=" + pt.deviceType,
        pt.redirect(pt.target, t)
    },
    submit: function(t) {
        var e = pt.uInput
          , n = pt.is_oversea ? $("password") : pt.pInput
          , i = ""
          , o = "";
        pt.is_qlogin ? i = pt.login_uin : (i = pt.needAt ? pt.needAt : e && e.value,
        pt.login_uin = i),
        t && (pt.submit_o.pt_vcode_v1 = 0,
        pt.submit_o.pt_verifysession_v1 = pt.verifysession),
        pt.submit_o.verifycode = pt.vcode.toUpperCase(),
        pt.submit_o.u = i;
        var r = !1;
        window.openSDK && openSDK.md5Pwd && 0 == openSDK.result ? (o = openSDK.md5Pwd,
        r = !0) : (o = n && n.value,
        r = !1),
        "open" != t && (pt.submit_o.p = $.Encryption.getEncryption(o, pt.salt, pt.submit_o.verifycode, r)),
        pt.submit_o.pt_randsalt = pt.isRandSalt || 0,
        pt.submit_o.ptlang = pt.lang_num,
        pt.submit_o.low_login_enable = 1 == pt.low_login_enable ? 1 : 0,
        pt.submit_o.low_login_enable && (pt.submit_o.low_login_hour = window.ptui_low_login_hour),
        pt.submit_o.u1 = encodeURIComponent(pt.s_url),
        pt.submit_o.from_ui = 1,
        pt.submit_o.fp = "loginerroralert",
        pt.submit_o.device = pt.deviceType,
        pt.submit_o.aid = pt.appid,
        window.ptui_daid && (pt.submit_o.daid = window.ptui_daid),
        "1" == window.ptui_pt_qzone_sig && (pt.submit_o.pt_qzone_sig = 1),
        "1" == window.ptui_pt_ttype && (pt.submit_o.pt_ttype = "1"),
        "1" == window.ptui_pt_light && (pt.submit_o.pt_light = "1"),
        window.ptui_pt_3rd_aid && (pt.submit_o.pt_3rd_aid = window.ptui_pt_3rd_aid),
        pt.submit_o.ptredirect = pt.target,
        pt.submit_o.h = 1,
        pt.submit_o.g = 1,
        pt.submit_o.pt_uistyle = window.ptui_style,
        "0" != window.ptui_kf_csimc && window.ptui_kf_csimc && (pt.submit_o.csimc = ptui_kf_csimc,
        pt.submit_o.csnum = ptui_kf_csnum,
        pt.submit_o.authid = ptui_kf_authid),
        pt.submit_o.regmaster = window.ptui_regmaster;
        var a = pt.object2param(pt.submit_o);
        if (t) {
            pt.checkNetwork(),
            pt.startLoading();
            var p = pt.isHulian ? "pt_open_login" : "login"
              , s = pt.loginUrl + p + "?" + a;
            $.http.loadScript(s)
        } else
            pt.showVcode(),
            pt.isSubmiting = !1;
        return !1
    },
    cb: function(t, e, n, i, o, r) {
        switch (pt.isSubmiting = !1,
        pt.qrcode.used && -1 == [0, 65, 66, 67].indexOf(+t) && $.report.monitor("2586869"),
        +t) {
        case 0:
            clearInterval(pt.qrcode.clock);
            var a = pt.uInput && pt.uInput.value;
            return pt.saveLastUin(a || ""),
            n.indexOf("/cgi-bin/mibao_vry") > -1 && (n += "&style=" + pt.style),
            pt.isOffice && window.mqq && mqq.invoke && mqq.invoke("QQOfficeOpen", "checkApp", {
                appId: window.ptui_pt_3rd_aid
            }),
            pt.qrcode.used && (pt.qrcode.done = !0,
            $.report.monitor("2136878")),
            void pt.redirect(i, n);
        case 4:
            pt.changeCodeImg();
            break;
        case 65:
            if (clearInterval(pt.qrcode.clock),
            $.report.monitor("2586868"),
            window.ptui_tab) {
                if ($.css.show($("qr_invalid")),
                "1" == $.bom.query("autorefresh"))
                    return void $("qr_invalid").click()
            } else
                pt.showErr("一键登录超时，请重试。", 1e6);
            return;
        case 66:
        case 67:
            return;
        default:
            clearInterval(pt.qrcode.clock),
            pt.go_back()
        }
        3 != t || pt.is_oversea || -1 === [9, 35, 42].indexOf(parseInt(window.ptui_style)) ? pt.showErr(o) : pt.showAlert(o + "<br>" + STR_LANG.password_error_tips)
    },
    cb_checkVC: function(t, e, n, i, o) {
        switch (t + "") {
        case "0":
            pt.vcode = e || "abcd",
            pt.verifysession = i;
            break;
        case "1":
            pt.vcode = "",
            pt.cap_cd = e
        }
        return 2 == t ? void pt.showErr(pt.lang.err_uin) : 3 == t ? void pt.showErr(pt.checkErr[ptui_lang]) : (pt.salt = n,
        pt.isRandSalt = o,
        void pt.submit(pt.vcode))
    },
    check: function(t) {
        if (!pt.isSubmiting) {
            if (pt.is_qlogin = t,
            !pt.is_qlogin && !pt.checkValidate())
                return clearTimeout(pt._timer),
                void pt.endLoading();
            var e = "";
            e = t ? pt.login_uin : pt.needAt ? pt.needAt : pt.uInput.value;
            var n = pt.checkUrl + "pt_tea=2&uin=" + e + "&appid=" + pt.appid + "&ptlang=" + pt.lang_num + "&regmaster=" + window.ptui_regmaster + "&pt_uistyle=" + pt.style + "&r=" + Math.random();
            window.TDC && TDC.getInfo && TDC.getInfo().tokenid && (n += "&pt_jstoken=" + TDC.getInfo().tokenid),
            $.http.loadScript(n)
        }
    },
    checkValidate: function() {
        var t = pt.is_oversea ? $("phone") : pt.uInput
          , e = pt.is_oversea ? $("password") : pt.pInput
          , n = t.value;
        return pt.is_oversea && (n = "00" + $("country-code").innerHTML.replace(/[^0-9]/, "") + n,
        n = n.replace("0086", "")),
        "" == t.value ? (pt.showErr(pt.lang.no_uin, function() {
            t.focus()
        }
        ),
        !1) : pt.checkQQUin(n) ? (t.value = $.str.trim(t.value),
        "" != e.value || (pt.showErr(pt.lang.no_password, function() {
            e.focus()
        }
        ),
        !1)) : (pt.showErr(pt.lang.err_uin, function() {
            t.focus()
        }
        ),
        !1)
    },
    checkQQUin: function(t) {
        if (0 == t.length)
            return !1;
        t = $.str.trim(t),
        pt.needAt = "";
        var e = $.check;
        return $.check.is_weibo_appid(pt.appid) ? !(!e.isQQ(t) && !e.isMail(t)) || (e.isNick(t) || e.isName(t) ? (pt.needAt = "@" + encodeURIComponent(t),
        !0) : e.isPhone(t) ? (pt.needAt = "@" + t.replace(/^(86|886)/, ""),
        !0) : e.isSeaPhone(t) ? (pt.needAt = "@00" + t.replace(/^(00)/, ""),
        /^(@0088609)/.test(pt.needAt) && (pt.needAt = pt.needAt.replace(/^(@0088609)/, "@008869")),
        !0) : (pt.needAt = "",
        e.isForeignPhone(t) ? (pt.needAt = "@" + t,
        !0) : !!e.isPaipaiDuokefu(t))) : !(!e.isQQ(t) && !t.match(/@/)) || (e.isPhone(t) ? (pt.needAt = "@" + t.replace(/^(86|886)/, ""),
        !0) : (e.isForeignPhone(t) && (pt.needAt = "@" + t),
        pt.uInput.value = t + "@qq.com",
        !0))
    },
    checkVcode: function() {
        var t = $("vcode_input");
        return "" == t.value ? (pt.showErr(pt.lang.no_code),
        t.focus(),
        !1) : t.value.length < 4 ? (pt.showErr(pt.lang.less_code),
        t.focus(),
        t.select(),
        !1) : !!/^[a-zA-Z0-9]+$/.test(t.value) || (pt.showErr(pt.lang.err_code),
        t.focus(),
        t.select(),
        !1)
    },
    clickHeader: function(t) {
        t.preventDefault();
        var e = t.currentTarget
          , n = e.getAttribute("id")
          , i = e.getAttribute("type");
        switch (pt.login_uin = n,
        pt.login_pwd = e.getAttribute("pwd"),
        i + "") {
        case "1":
            pt.check(!0);
            break;
        case "2":
            pt.qlogin_submit();
            break;
        case "3":
            pt.authlogin_submit();
            break;
        case "4":
            pt.cookielogin_submit();
            break;
        default:
            pt.check(!0)
        }
    },
    setHeader: function(t) {
        for (var e in t)
            "" != t[e].url && "" != e && $("img_" + e) && ($("img_" + e).src = t[e]);
        pt.hideURLBar()
    },
    initFace: function() {
        for (var t = pt.qlogin_list_data, e = t.length, n = pt.isHttps ? "https://ssl." : "http://", i = 0; i < e; i++)
            $.http.loadScript(n + "ptlogin2." + pt.domain + "/getface?appid=" + pt.appid + "&imgtype=3&encrytype=0&devtype=1&keytpye=0&uin=" + t[i].uin + "&r=" + Math.random())
    },
    face_error: function(t) {
        return t.src != pt.default_face_url && (t.src = pt.default_face_url),
        !1
    },
    getShortWord: function(t, e, n) {
        e = e || "";
        if (t.innerHTML = e,
        t.clientWidth <= n)
            ;
        else
            for (var i = e.length, o = Math.ceil(i / 2), r = 0; r < o; r++) {
                var a = e.substring(0, o - r)
                  , p = e.substring(o + r, i);
                if (t.innerHTML = a + "..." + p,
                t.clientWidth <= n) {
                    t.title = e;
                    break
                }
                var p = e.substring(o + r + 1, i);
                if (t.innerHTML = a + "..." + p,
                t.clientWidth <= n) {
                    t.title = e;
                    break
                }
            }
        t.style.width = n + "px"
    },
    changeCodeImg: function() {
        if (pt.new_vcode)
            ;
        else {
            var t = $("vcode_img")
              , e = pt.domain
              , n = (pt.isHttps ? "https://ssl." : "http://") + "captcha." + e + "/getimage";
            pt.isHttps && "qq.com" != e && "tenpay.com" != e && (n = "https://ssl.ptlogin2." + e + "/ptgetimage"),
            n += "?aid=" + pt.appid + "&uin=" + pt.login_uin + "&v=" + Math.random(),
            t.src = n
        }
    },
    newVCFirst: !0,
    showVcode: function() {
        pt.new_vcode ? ($("content").style.display = "none",
        $("new_vcode").style.display = "block",
        pt.newVCFirst ? (pt.newVCFirst = !1,
        capInit($("new_vcode"), {
            callback: pt.VCCallback,
            showHeader: !0,
            uin: pt.login_uin,
            capcd: pt.cap_cd
        })) : capRefresh({
            uin: pt.login_uin,
            capcd: pt.cap_cd
        }),
        pt.ptui_notifySize()) : ($("login").style.display = "none",
        $("vcode").style.display = "block",
        pt.bindVcodeEvent(),
        pt.changeCodeImg()),
        pt.hideURLBar(),
        $("btn_app_down") && $.css.hide("btn_app_down")
    },
    go_back: function() {
        $("content") && ($("content").style.display = "block"),
        $("login") && ($("login").style.display = "block"),
        $("vcode") && ($("vcode").style.display = "none"),
        $("new_vcode") && ($("new_vcode").style.display = "none")
    },
    submitVcode: function() {
        if (!pt.isSubmiting) {
            if (!pt.checkVcode())
                return !1;
            pt.submit_o.verifycode = $("vcode_input").value.toUpperCase();
            var t = ""
              , e = !1;
            window.openSDK && openSDK.md5Pwd && 0 == openSDK.result ? (t = openSDK.md5Pwd,
            e = !0) : (t = pt.pInput.value,
            e = !1),
            pt.submit_o.p = $.Encryption.getEncryption(t, pt.salt, pt.submit_o.verifycode, e),
            pt.submit_o.pt_randsalt = pt.isRandSalt || 0,
            window.TDC && TDC.getInfo && TDC.getInfo().tokenid && (pt.submit_o.pt_jstoken = TDC.getInfo().tokenid);
            var n = pt.object2param(pt.submit_o)
              , i = pt.isHulian ? "pt_open_login" : "login"
              , o = (pt.isHttps ? "https://ssl." : "http://") + "ptlogin2." + pt.domain + "/" + i + "?" + n;
            $.http.loadScript(o)
        }
    },
    submitNewVcode: function(t) {
        pt.submit_o.verifycode = t.randstr.toUpperCase(),
        pt.submit_o.pt_vcode_v1 = 1,
        pt.submit_o.pt_verifysession_v1 = t.ticket;
        var e = ""
          , n = !1;
        window.openSDK && openSDK.md5Pwd && 0 == openSDK.result ? (e = openSDK.md5Pwd,
        n = !0) : (e = pt.pInput.value,
        n = !1),
        pt.submit_o.p = $.Encryption.getEncryption(e, pt.salt, pt.submit_o.verifycode, n),
        pt.submit_o.pt_randsalt = pt.isRandSalt || 0,
        "0" != window.ptui_kf_csimc && window.ptui_kf_csimc && (pt.submit_o.csimc = ptui_kf_csimc,
        pt.submit_o.csnum = ptui_kf_csnum,
        pt.submit_o.authid = ptui_kf_authid);
        var i = pt.object2param(pt.submit_o)
          , o = pt.isHulian ? "pt_open_login" : "login"
          , r = (pt.isHttps ? "https://ssl." : "http://") + "ptlogin2." + pt.domain + "/" + o + "?" + i;
        pt.checkNetwork(),
        pt.startLoading(),
        $.http.loadScript(r)
    },
    open: {
        timer: -1,
        authListDone: !1,
        waiting: function(t, e) {
            if (e)
                switch (t) {
                case "authlist":
                    pt.open.authListDone ? e() : pt.open.waiting.authlistFn = e;
                    break;
                case "authdata":
                    pt.submit_o.openlogin_data ? e() : (pt.open.getAuthData(),
                    pt.open.waiting.authdataFn = e)
                }
        },
        authListReady: function(t) {
            pt.open.authListDone = !0,
            pt.open.waiting.authlistFn && (pt.open.waiting.authlistFn(),
            pt.open.waiting.authlistFn = null ),
            t = t || {},
            window.ptui_daid && parseInt($.cookie.get("supertoken")) && (t.superkey = 1),
            t.pt_flex && (window.pt_flex = 1),
            t.skey && (t.skey = +t.skey),
            window.pt_skey_valid = t.skey,
            (1 == t.skey || t.superkey || pt.qqBrowserInfo) && (pt.openCookieInfo = t,
            pt.refreshQloginUI(),
            pt.initFace()),
            window.pt_flex ? pt.open.getData({
                value: location.search.substr(1) + "&pt_flex=1"
            }) : xMsg.call("connect", "getData", {}, pt.open.getData),
            window.ptui_tab && xMsg.call("connect", "hideList", {})
        },
        setFrameHeight: function(t) {},
        getData: function(t) {
            if (clearTimeout(pt.open.timer),
            pt.submit_o.openlogin_data = encodeURIComponent(t.value),
            pt.open.waiting.authdataFn)
                return pt.open.waiting.authdataFn(),
                void (pt.open.waiting.authdataFn = null )
        },
        getAuthData: function() {
            if (!pt.open.authListDone)
                return pt.showErr("授权列表加载失败");
            pt.open.timer = setTimeout(function() {
                pt.showErr("授权信息获取失败")
            }
            , 3e3)
        },
        fillAppInfo: function(t) {
            if (t && 0 == t.retcode && t.result) {
                var e = t.result.IconUrl;
                e && e.indexOf("http://") > -1 && (e = e.replace("http://", "https://")),
                $("app_logo").style.backgroundImage = "url(" + e + ")",
                $("app_alias").innerHTML = t.result.AppAlias,
                $("app_comment").innerHTML = t.result.AppComment
            }
        },
        loadAppInfo: function() {
            $.http.loadScript("//cgi.connect.qq.com/qqconnectwebsite/get_app_basicinfo?appid=" + window.ptui_pt_3rd_aid + "&platform_type=1&env=1&callback=get_app_basicinfo")
        }
    },
    crossMessage: function(t) {
        if (void 0 !== window.postMessage) {
            var e = $.str.json2str(t);
            window.parent.postMessage(e, "*")
        }
    },
    ptui_notifyClose: function(t) {
        t && t.preventDefault();
        var e = {};
        e.action = "close",
        pt.crossMessage(e)
    },
    ptui_notifySize: function(t) {
        var e = {};
        if (e.action = "resize",
        t) {
            var n = $(t);
            e.width = n.offsetWidth || 1,
            e.height = n.offsetHeight || 1
        } else
            e.width = 320,
            e.height = 441;
        pt.crossMessage(e)
    },
    accessCount: function() {
        return $.localStorage.isSupport() ? parseInt($.localStorage.get("accessCount")) : 0
    },
    access: function() {
        if ($.localStorage.isSupport())
            try {
                var t, e, n;
                n = new Date,
                e = new Date,
                e.setTime($.localStorage.get("lastAccessDate")),
                t = Math.abs(n - e) < 3e4 ? parseInt($.localStorage.get("accessCount")) + 1 : 1,
                $.localStorage.set("accessCount", t),
                $.localStorage.set("lastAccessDate", n.getTime())
            } catch (t) {
                $.localStorage.set("accessCount", 1),
                $.localStorage.set("lastAccessDate", (new Date).getTime())
            }
    },
    switchpwd: function() {
        $.css.show($("pwdlogin")),
        pt.uInput && pt.uInput.focus(),
        $.css.hide($("qrlogin")),
        $.css.show($("switch")),
        $.css.show($("zc_feedback")),
        clearInterval(pt.qrcode.clock)
    },
    switchqr: function() {
        $.css.hide($("pwdlogin")),
        $.css.show($("qrlogin")),
        $.css.hide($("switch")),
        $.css.hide($("zc_feedback")),
        pt.qrcode.get(0)
    },
    refreshQloginUI: function() {
        pt.isOffice ? pt.build_office_qlogin() : pt.isWtlogin || pt.is3gNews || pt.build_qlogin_list()
    }
};
pt.access(),
pt.init(),
"0" !== $.bom.query("pt_wxtest") && pt.isWX && weixin_sig_cb(),
pt.qrcode = {
    CGI: "ptqrlogin",
    used: !1,
    done: !1,
    clock: 0,
    get: function(t) {
        var e = pt.isHttps ? "https://ssl." : "http://"
          , n = e + "ptlogin2." + pt.domain + "/ptqrshow?s=8&e=0&";
        n += "appid=" + pt.appid + "&type=" + t + "&t=" + Math.random(),
        pt.daid && (n += "&daid=" + pt.daid),
        window.ptui_regmaster && (n += "&regmaster=" + window.ptui_regmaster),
        clearInterval(pt.qrcode.clock),
        pt.checkNetwork(),
        1 == t ? $.http.loadScript(n) : ($.e.add($("qrimg"), "error", function() {
            $.css.show("qr_invalid")
        }
        ),
        $.e.add($("qrimg"), "load", function() {
            clearTimeout(pt._timer),
            pt.qrcode.polling()
        }
        ),
        $("qrimg").src = n),
        pt.qrcode.used = !0,
        pt.qrcode.done = !1,
        $.report.monitor("2136877")
    },
    polling: function(t) {
        if (clearInterval(pt.qrcode.clock),
        pt.qrcode.clock = setInterval(function() {
            var t = pt.qlogin_submit(pt.qrcode.CGI);
            $.http.loadScript(t + "&r=" + Math.random())
        }
        , 3e3),
        t) {
            openApp((pt.isIPhone ? "wtloginmqq3:" : "wtloginmqq:") + "//ptlogin/qlogin?qrcode=" + encodeURIComponent(t) + "&schemacallback=" + encodeURIComponent("weixin://"))
        }
    }
},
OneKey.ERRMSG = {
    2052: "使用一键登录，<a href='http://im.qq.com/mobileqq/touch/53/index.html' target='_blank'>请安装最新版本的QQ手机版</a>",
    1028: "使用一鍵登錄，<a href='http://im.qq.com/mobileqq/touch/53/index.html' target='_blank'>請安裝最新版本的QQ手機版</a>",
    1033: "Have <a href='http://im.qq.com/mobileqq/touch/53/index.html' target='_blank'>the latest Mobile QQ</a>？"
};
var openSDK = function() {
    var t = 0
      , e = []
      , n = function(n, i) {
        t = 1,
        "function" == typeof i && (e[t] = i),
        window.location.href = "jsbridge://SecureJsInterface/curPosFromJS/" + t + "/openSDKCallBack/" + n
    }
      , i = function(n, i) {
        t = 2,
        "function" == typeof i && (e[t] = i),
        window.location.href = "jsbridge://SecureJsInterface/isPasswordEdit/" + t + "/openSDKCallBack/" + n
    }
      , o = function(n) {
        t = 3,
        "function" == typeof n && (e[t] = n),
        window.location.href = "jsbridge://SecureJsInterface/clearAllEdit/" + t + "/openSDKCallBack"
    }
      , r = function(n) {
        t = 4,
        "function" == typeof n && (e[t] = n),
        window.location.href = "jsbridge://SecureJsInterface/getMD5FromNative/" + t + "/openSDKCallBack"
    }
    ;
    if ("1" == window.ptui_enablePwd)
        return {
            curPosFromJS: n,
            isPasswordEdit: i,
            clearAllEdit: o,
            getMD5FromNative: r,
            sn: t,
            md5Pwd: "",
            result: 0,
            callbackArray: e
        }
}
();
