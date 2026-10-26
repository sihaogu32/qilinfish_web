/* qilinfish.js — small client-side helpers for Thymeleaf views.
   Naming convention follows project rule: function args prefixed with __. */

(function () {
  'use strict';

  /**
   * Page text this script writes itself, in both languages. The page sets <html lang="en"> under /en
   * (LangModelAdvice), so the language is read from there; anything else is Chinese.
   * Amount messages must say the same as i18n/survey*.properties, which SurveyController shows after a POST.
   */
  var TEXT = {
    zh: {
      contactError: '> error: 請檢查必填欄位 / please review the highlighted fields',
      amountInvalid: '> 金額只能填數字（不加幣別），例如 500 或 1,200.5。',
      amountTooLong: '> 金額最多 16 位數。',
      payRequired: '> 請填金額範圍，或選「不會付費」「不知道」。',
      payPartial: '> 最低和最高都要填；如果是固定金額，兩格填一樣。',
      payRange: '> 最高金額不可小於最低金額。',
      surveyError: '> 還有必填的題目沒填，請看標紅的地方。',
      picked: '● 已選',
      pick: '選這個',
      partVerify: '最近一次，您確認 AI 產出的那一次',
      partExplain: '最近一次，有人問您「這是怎麼來的」',
      partInventory: '最近一次，您需要弄清楚舊系統的那一次',
      partOther: '最近一次，您需要處理的那一件事',
      partNone: '如果都沒遇過，也請告訴我們',
      countUnit: ' 字',
      countHint: ' · 幾句話就好'
    },
    en: {
      contactError: '> error: please review the highlighted fields',
      amountInvalid: '> Amounts must be numbers only (no currency), e.g. 500 or 1,200.5.',
      amountTooLong: '> Amounts can have at most 16 digits.',
      payRequired: '> Please fill in a range, or pick "Would not pay" or "Don\'t know".',
      payPartial: '> Both minimum and maximum are needed; for a fixed amount, fill in the same number in both.',
      payRange: '> The maximum cannot be less than the minimum.',
      surveyError: '> Some required questions are still unanswered. Please check the ones marked in red.',
      picked: '● Picked',
      pick: 'Pick this',
      partVerify: 'The last time you checked AI output',
      partExplain: 'The last time someone asked you "where did this come from?"',
      partInventory: 'The last time you had to work out a legacy system',
      partOther: 'The last time you had to deal with one of these',
      partNone: 'If it never happened to you, please tell us too',
      countUnit: ' characters',
      countHint: ' · a few sentences is enough'
    }
  };
  var T = TEXT[(document.documentElement.lang || '').slice(0, 2) === 'en' ? 'en' : 'zh'];

  /** Reveal-on-scroll for elements tagged with [data-reveal]. */
  function initReveal() {
    var nodes = document.querySelectorAll('[data-reveal]');
    if (!('IntersectionObserver' in window)) {
      nodes.forEach(function (n) { n.classList.add('is-revealed'); });
      return;
    }
    var io = new IntersectionObserver(function (__entries) {
      __entries.forEach(function (e) {
        if (e.isIntersecting) {
          e.target.classList.add('is-revealed');
          io.unobserve(e.target);
        }
      });
    }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
    nodes.forEach(function (n) { io.observe(n); });
  }

  /** Wraps every visible character of __el in a .tw-char span, keeping the
      element's own markup (phrase spans, spaces) so the line breaks stay put. */
  function splitChars(__el) {
    var chars = [];
    var walker = document.createTreeWalker(__el, NodeFilter.SHOW_TEXT);
    var texts = [];
    while (walker.nextNode()) texts.push(walker.currentNode);
    texts.forEach(function (t) {
      var frag = document.createDocumentFragment();
      Array.from(t.nodeValue).forEach(function (ch) {
        if (/\s/.test(ch)) { frag.appendChild(document.createTextNode(ch)); return; }
        var span = document.createElement('span');
        span.className = 'tw-char';
        span.textContent = ch;
        frag.appendChild(span);
        chars.push(span);
      });
      t.parentNode.replaceChild(frag, t);
    });
    return chars;
  }

  /** Types out a [data-typewriter] element's own text once, when it scrolls into view.
      The text keeps its space while hidden, so nothing below it moves. */
  function typeOnce(__el) {
    var chars = splitChars(__el);
    if (!chars.length) return;
    __el.classList.add('is-typing');
    // 50–100 ms a character; text longer than 62 characters (the Chinese WHAT sentence) types faster, so English
    // takes about as long as Chinese
    var step = Math.min(50, 3100 / chars.length);
    var i = 0;
    (function tick() {
      if (i > 0) chars[i - 1].classList.remove('is-caret');
      chars[i].classList.add('is-on', 'is-caret');
      i++;
      if (i < chars.length) setTimeout(tick, step + Math.random() * step);
    })();
  }

  function initTypewriter() {
    var nodes = document.querySelectorAll('[data-typewriter]');
    if (!nodes.length) return;
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    if (!('IntersectionObserver' in window)) return;
    var io = new IntersectionObserver(function (__entries) {
      __entries.forEach(function (e) {
        if (e.isIntersecting) { io.unobserve(e.target); typeOnce(e.target); }
      });
    }, { threshold: 0.6 });
    nodes.forEach(function (n) { io.observe(n); });
  }

  /** Simple front-end form validation for the contact form. */
  function initContactForm() {
    var form = document.querySelector('[data-contact-form]');
    if (!form) return;
    var statusEl = form.querySelector('[data-form-status]');

    form.addEventListener('submit', function (e) {
      var ok = true;
      form.querySelectorAll('.qf-field').forEach(function (f) {
        f.classList.remove('is-invalid');
        var input = f.querySelector('input, textarea');
        if (!input) return;
        var v = (input.value || '').trim();
        if (input.required && !v) { f.classList.add('is-invalid'); ok = false; return; }
        if (input.type === 'email' && v && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) {
          f.classList.add('is-invalid'); ok = false;
        }
      });
      if (!ok) {
        e.preventDefault();
        if (statusEl) {
          statusEl.className = 'qf-form-status err';
          statusEl.textContent = T.contactError;
        }
      }
    });
  }

  /** Lang switcher — the links already point at the other language; remember the pick so "/" opens it next time. */
  function initLangSwitcher() {
    document.querySelectorAll('[data-lang]').forEach(function (link) {
      link.addEventListener('click', function () {
        document.cookie = 'qf_lang=' + link.getAttribute('data-lang') + '; path=/; max-age=31536000; samesite=lax';
      });
    });
  }

  /**
   * Amount field check: '' when fine, otherwise the message to show.
   * A picked choice ("none", "unknown") stands in for the amounts. Filled boxes must be plain numbers;
   * a required field needs every box, and with two boxes the second (max) must not be below the first (min).
   */
  /**
   * An amount as the server reads it (SurveyFormModel.plainUsd): full-width digits, comma and point
   * become half-width, then commas and spaces are dropped.
   */
  function plainAmount(__v) {
    return __v.replace(/[０-９，．]/g, function (c) { return String.fromCharCode(c.charCodeAt(0) - 0xFEE0); })
              .replace(/[,\s]/g, '');
  }

  function amountError(__field) {
    if (__field.querySelector('input[type="radio"]:checked')) return '';
    var vals = Array.prototype.map.call(__field.querySelectorAll('[data-amount]'), function (b) { return plainAmount(b.value); });
    var filled = vals.filter(Boolean);
    if (filled.some(function (v) { return !/^\d+(\.\d+)?$/.test(v); })) return T.amountInvalid;
    // the limit counts digits only, so commas never push an amount over it
    if (filled.some(function (v) { return v.replace('.', '').length > 16; })) return T.amountTooLong;
    if (!__field.hasAttribute('data-required')) return '';
    if (!filled.length) return T.payRequired;
    if (filled.length < vals.length) return T.payPartial;
    if (vals.length > 1 && compareDecimal(vals[1], vals[0]) < 0) return T.payRange;
    return '';
  }

  /**
   * Compares two plain decimal strings ("1200.5") without going through Number, so long amounts keep
   * every digit, same as the server's BigDecimal. Returns <0, 0 or >0.
   */
  function compareDecimal(__a, __b) {
    function parts(__s) {
      var p = __s.split('.');
      return [p[0].replace(/^0+/, ''), (p[1] || '').replace(/0+$/, '')];
    }
    var a = parts(__a), b = parts(__b);
    if (a[0].length !== b[0].length) return a[0].length - b[0].length;
    var n = Math.max(a[1].length, b[1].length);
    var x = a[0] + a[1].padEnd(n, '0'), y = b[0] + b[1].padEnd(n, '0');
    return x < y ? -1 : (x > y ? 1 : 0);
  }

  /** An email or a phone number; shared by the submit check and the section index ticks. */
  function isValidContact(__v) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(__v) || /^\+?[0-9()\-\s#]{7,24}$/.test(__v);
  }

  /** Survey form: show the branch that matches the answers, validate visible required fields. */
  function initSurveyForm() {
    var form = document.querySelector('[data-survey-form]');
    if (!form) return;
    var statusEl = form.querySelector('[data-form-status]');

    function checkedValue(__name) {
      var el = form.querySelector('input[name="' + __name + '"]:checked');
      return el ? el.value : '';
    }

    function syncBranches() {
      var situation = checkedValue('situation');
      form.querySelectorAll('[data-branch]').forEach(function (b) {
        var want = b.getAttribute('data-branch');
        b.hidden = !situation || (want === 'none' ? situation !== 'none' : situation === 'none');
      });
      // "name=value" shows on that answer, "name!=value" on any other answer (including none yet)
      form.querySelectorAll('[data-show-if]').forEach(function (b) {
        var rule = b.getAttribute('data-show-if').match(/^(\w+)(!?=)(.*)$/);
        var same = checkedValue(rule[1]) === rule[3];
        b.hidden = rule[2] === '=' ? !same : same;
      });
    }

    function clearMark(__e) {
      var f = __e.target.closest('.qf-field');
      if (f) f.classList.remove('is-invalid');
    }

    // typing in the "other" box picks "other", since the box lives inside that choice
    form.querySelectorAll('[data-other-text]').forEach(function (t) {
      function pickOther() {
        var radio = t.closest('.qf-choice').querySelector('input[type="radio"]');
        if (radio.checked) return;
        radio.checked = true;
        radio.dispatchEvent(new Event('change', { bubbles: true }));
      }
      // not on focus: tabbing through the box must not change the answer
      ['click', 'input'].forEach(function (__type) { t.addEventListener(__type, pickOther); });
    });
    // an amount and the "no amount" choices answer the same question, so one clears the other
    form.querySelectorAll('.qf-field[data-amounts]').forEach(function (f) {
      var boxes = f.querySelectorAll('[data-amount]');
      var radios = f.querySelectorAll('input[type="radio"]');
      boxes.forEach(function (b) {
        b.addEventListener('input', function () {
          // only blanks or commas is not an amount yet, so the picked choice stays
          if (plainAmount(b.value)) radios.forEach(function (r) { r.checked = false; });
        });
      });
      radios.forEach(function (r) {
        r.addEventListener('change', function () {
          if (r.checked) boxes.forEach(function (b) { b.value = ''; });
        });
      });
    });
    form.addEventListener('change', syncBranches);
    form.addEventListener('change', clearMark);
    form.addEventListener('input', clearMark);
    syncBranches();
    // run every listener that mirrors the answers (branches, picker cards, index ticks, length hints);
    // the form event never reaches the "other" box, so it cannot pick "other" by itself
    function resyncAll() {
      form.dispatchEvent(new Event('change'));
      form.querySelectorAll('[data-sv-count-text]').forEach(function (t) { t.dispatchEvent(new Event('input')); });
    }
    // going back can refill the answers after this script ran, so mirror them again once the page shows
    window.addEventListener('pageshow', resyncAll);
    if (statusEl && /\b(ok|err)\b/.test(statusEl.className)) {
      statusEl.scrollIntoView({ block: 'center' });
    }

    form.addEventListener('submit', function (e) {
      var ok = true;
      form.querySelectorAll('.qf-field[data-required]').forEach(function (f) {
        f.classList.remove('is-invalid');
        if (f.closest('[hidden]') || f.hasAttribute('data-amounts')) return;
        var radios = f.querySelectorAll('input[type="radio"]');
        if (radios.length) {
          var picked = f.querySelector('input[type="radio"]:checked');
          var other = f.querySelector('[data-other-text]');
          // "other" needs its own words, otherwise the answer says nothing
          if (!picked || (other && picked.value === 'other' && !other.value.trim())) { f.classList.add('is-invalid'); ok = false; }
          return;
        }
        var input = f.querySelector('input, textarea');
        var v = input ? (input.value || '').trim() : '';
        if (!v) { f.classList.add('is-invalid'); ok = false; return; }
        if (input.hasAttribute('data-contact') && !isValidContact(v)) { f.classList.add('is-invalid'); ok = false; }
      });
      // amount fields say which rule failed, so the message is set per field
      form.querySelectorAll('.qf-field[data-amounts]').forEach(function (f) {
        f.classList.remove('is-invalid');
        if (f.closest('[hidden]')) return;
        var msg = amountError(f);
        if (!msg) return;
        f.querySelector('.qf-field__err').textContent = msg;
        f.classList.add('is-invalid');
        ok = false;
      });
      if (!ok) {
        e.preventDefault();
        if (statusEl) {
          statusEl.className = 'qf-form-status err';
          statusEl.textContent = T.surveyError;
        }
        var first = form.querySelector('.qf-field.is-invalid');
        if (first) first.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    });
  }

  /** Run __fn once when __el scrolls into view (immediately without IntersectionObserver). */
  function whenSeen(__el, __threshold, __fn) {
    if (!('IntersectionObserver' in window)) { __fn(); return; }
    var io = new IntersectionObserver(function (__entries) {
      if (__entries[0].isIntersecting) { io.disconnect(); __fn(); }
    }, { threshold: __threshold });
    io.observe(__el);
  }

  /** Survey fig.1: 2,207 dots light up one by one, the counter follows. */
  function initSurveyDots() {
    var canvas = document.querySelector('[data-sv-dots]');
    if (!canvas) return;
    var ctx = canvas.getContext('2d');
    var counter = document.querySelector('[data-sv-count]');
    var total = 2207, cols = 78;
    if (canvas.clientWidth && canvas.clientWidth < 600) {
      // narrow screens: a taller grid so the dots stay visible instead of a thin strip
      canvas.width = 720; canvas.height = 860; cols = 42;
    }
    var rows = Math.ceil(total / cols);
    var w = canvas.width, h = canvas.height, pad = 26;
    var gx = (w - pad * 2) / cols, gy = (h - pad * 2) / rows;
    var order = [];
    for (var i = 0; i < total; i++) order.push(i);
    for (var j = total - 1; j > 0; j--) {
      var k = Math.floor(Math.random() * (j + 1)), t = order[j];
      order[j] = order[k]; order[k] = t;
    }

    function drawDot(__i, __color, __r) {
      ctx.fillStyle = __color;
      ctx.beginPath();
      ctx.arc(pad + (__i % cols + 0.5) * gx, pad + (Math.floor(__i / cols) + 0.5) * gy, __r, 0, Math.PI * 2);
      ctx.fill();
    }
    function drawAll(__color) {
      ctx.fillStyle = '#0f0f12';
      ctx.fillRect(0, 0, w, h);
      for (var i = 0; i < total; i++) drawDot(i, __color, 2.4);
    }

    drawAll('#6f8fd8');
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

    function run() {
      var lit = 0, start = null, dur = 5600;
      drawAll('#1c1c22');
      function frame(__ts) {
        if (start === null) start = __ts;
        var p = Math.min(1, (__ts - start) / dur);
        var target = Math.floor((1 - Math.pow(1 - p, 2.2)) * total);
        for (; lit < target; lit++) drawDot(order[lit], '#e8e8ec', 2.8);
        for (var b = Math.max(0, lit - 60); b < lit - 10; b++) drawDot(order[b], '#6f8fd8', 2.4);
        if (counter) counter.textContent = target.toLocaleString('en-US');
        if (p < 1) { requestAnimationFrame(frame); return; }
        drawAll('#6f8fd8');
        if (counter) counter.textContent = '2,207';
        setTimeout(run, 5000);
      }
      requestAnimationFrame(frame);
    }
    whenSeen(canvas, 0.35, run);
  }

  /** Survey figures, picker cards, section index and answer-length hints. */
  function initSurveyVisuals() {
    document.querySelectorAll('[data-sv-grow]').forEach(function (el) {
      whenSeen(el, 0.3, function () { el.classList.add('is-grown'); });
    });

    // looping clips play only while on screen; reduced motion keeps the poster still
    var clips = document.querySelectorAll('video[data-sv-play]');
    var still = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (clips.length && !still && 'IntersectionObserver' in window) {
      var vio = new IntersectionObserver(function (__entries) {
        __entries.forEach(function (e) {
          if (e.isIntersecting) {
            var p = e.target.play();
            if (p && p.catch) p.catch(function () {});
          } else {
            e.target.pause();
          }
        });
      }, { threshold: 0.25 });
      clips.forEach(function (v) { vio.observe(v); });
    }

    var form = document.querySelector('[data-survey-form]');
    if (!form) return;
    var art = {
      verify:    { img: 'bp_desk.jpg',      title: T.partVerify },
      explain:   { img: 'bp_chain.jpg',     title: T.partExplain },
      inventory: { img: 'bp_mainframe.jpg', title: T.partInventory },
      other:     { img: 'bp_desk.jpg',      title: T.partOther },
      none:      { img: 'bp_desk.jpg',      title: T.partNone }
    };
    var partImg = form.querySelector('[data-sv-part-img]');
    var partTitle = form.querySelector('[data-sv-part-title]');
    var picks = document.querySelectorAll('[data-sv-pick]');
    // the page's own picture and title, shown again when nothing is picked (e.g. after a reset)
    var partImgName = partImg && partImg.src.match(/[^\/]+\.jpg$/);
    var partDefault = partImgName ? { img: partImgName[0], title: partTitle.textContent } : null;

    function syncPicked() {
      var el = form.querySelector('input[name="situation"]:checked');
      var v = el ? el.value : '';
      picks.forEach(function (p) {
        var on = p.getAttribute('data-sv-pick') === v;
        if (p.hasAttribute('aria-pressed')) p.setAttribute('aria-pressed', on ? 'true' : 'false');
        var st = p.querySelector('.st');
        if (st) st.textContent = on ? T.picked : T.pick;
      });
      var shown = art[v] || partDefault;
      if (shown && partImg) {
        partImg.src = partImg.src.replace(/[^\/]+\.jpg$/, shown.img);
        partTitle.textContent = shown.title;
      }
      // parts 03–04 only exist on the story branch, which stays hidden until a situation other than "none" is picked
      document.querySelectorAll('[data-branch-link]').forEach(function (a) { a.hidden = !v || v === 'none'; });
    }

    picks.forEach(function (p) {
      p.addEventListener('click', function () {
        var radio = form.querySelector('input[name="situation"][value="' + p.getAttribute('data-sv-pick') + '"]');
        if (!radio) return;
        radio.checked = true;
        radio.dispatchEvent(new Event('change', { bubbles: true }));
        document.getElementById('sv-form').scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    });
    form.addEventListener('change', syncPicked);
    syncPicked();

    // /survey?pick=verify|explain|inventory (from the products page cards): same as clicking that card; other values are ignored
    var preset = new URLSearchParams(location.search).get('pick');
    if (['verify', 'explain', 'inventory'].indexOf(preset) >= 0) {
      var presetBtn = document.querySelector('[data-sv-pick="' + preset + '"]');
      if (presetBtn) presetBtn.click();
    }

    // section index: highlight the part on screen, tick parts whose required fields are answered
    var links = Array.prototype.slice.call(document.querySelectorAll('[data-sv-toc] a'));
    var parts = links.map(function (a) { return document.querySelector(a.getAttribute('href')); });
    var required = Array.prototype.slice.call(form.querySelectorAll('.qf-field[data-required]'));

    function partOf(__field) {
      var idx = -1;
      parts.forEach(function (p, i) {
        if (p && (p.compareDocumentPosition(__field) & Node.DOCUMENT_POSITION_FOLLOWING)) idx = i;
      });
      return idx;
    }
    function answered(__field) {
      if (__field.hasAttribute('data-amounts')) return !amountError(__field);
      if (__field.querySelector('input[type="radio"]')) {
        var picked = __field.querySelector('input[type="radio"]:checked');
        var other = __field.querySelector('[data-other-text]');
        return !!picked && !(other && picked.value === 'other' && !other.value.trim());
      }
      var input = __field.querySelector('input, textarea');
      var v = input ? input.value.trim() : '';
      if (v && input.hasAttribute('data-contact')) return isValidContact(v);
      return !!v;
    }
    // any answer at all, for parts with no required field (03): ticking says "I've been through this part"
    function touched(__field) {
      return Array.prototype.some.call(__field.querySelectorAll('input, textarea'), function (el) {
        return el.type === 'radio' ? el.checked : !!el.value.trim();
      });
    }
    var fields = Array.prototype.slice.call(form.querySelectorAll('.qf-field'));
    function syncDone() {
      links.forEach(function (a, i) {
        var inPart = function (f) { return partOf(f) === i && !f.closest('[hidden]'); };
        var fs = required.filter(inPart);
        a.classList.toggle('is-done', fs.length > 0 ? fs.every(answered) : fields.filter(inPart).some(touched));
      });
    }
    form.addEventListener('input', syncDone);
    form.addEventListener('change', syncDone);
    syncDone();

    if ('IntersectionObserver' in window) {
      var io = new IntersectionObserver(function (__entries) {
        __entries.forEach(function (e) {
          if (!e.isIntersecting) return;
          var i = parts.indexOf(e.target);
          links.forEach(function (a, k) { a.classList.toggle('is-now', k === i); });
        });
      }, { rootMargin: '-20% 0px -70% 0px' });
      parts.forEach(function (p) { if (p) io.observe(p); });
    }

    // answer-length hint under the long questions
    form.querySelectorAll('[data-sv-count-text]').forEach(function (t) {
      var out = t.parentNode.querySelector('.sv-cnt');
      if (!out) return;
      function sync() {
        var n = t.value.replace(/\s/g, '').length;
        out.textContent = n + T.countUnit + (n < 20 ? T.countHint : '');
      }
      t.addEventListener('input', sync);
      sync();
    });
  }

  /** Products opening: the lens drifts over the report in a slow figure-eight; reduced motion parks it. */
  function initProductsLens() {
    var lens = document.querySelector('[data-pd-lens]');
    if (!lens) return;
    // lens centre, in percent of the picture
    function pos(__t) { return { x: 47 + 9 * Math.sin(__t), y: 55 + 7 * Math.sin(2 * __t) }; }
    function place(__p) {
      lens.style.setProperty('--x', __p.x + '%');
      lens.style.setProperty('--y', __p.y + '%');
    }
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) { place(pos(0.6)); return; }
    var t0 = performance.now();
    (function frame(__now) {
      place(pos((__now - t0) / 2600));
      requestAnimationFrame(frame);
    })(t0);
  }

  document.addEventListener('DOMContentLoaded', function () {
    initReveal();
    initTypewriter();
    initContactForm();
    initSurveyForm();
    initSurveyDots();
    initSurveyVisuals();
    initProductsLens();
    initLangSwitcher();
  });
})();
