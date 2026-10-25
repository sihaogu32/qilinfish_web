/* qilinfish.js — small client-side helpers for Thymeleaf views.
   Naming convention follows project rule: function args prefixed with __. */

(function () {
  'use strict';

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

  /** Typewriter loop driven by [data-typewriter] containing | -separated phrases. */
  function initTypewriter() {
    var el = document.querySelector('[data-typewriter]');
    if (!el) return;
    var phrases = (el.getAttribute('data-typewriter') || '').split('|').map(function (s) { return s.trim(); }).filter(Boolean);
    if (!phrases.length) return;

    var idx = 0, cur = '', mode = 'type', step = 0;

    function tick() {
      var phrase = phrases[idx];
      if (mode === 'type') {
        step++;
        cur = phrase.slice(0, step);
        el.textContent = cur;
        if (step >= phrase.length) { mode = 'hold'; setTimeout(tick, 2400); return; }
        setTimeout(tick, 50 + Math.random() * 50);
      } else if (mode === 'hold') {
        mode = 'erase';
        setTimeout(tick, 0);
      } else if (mode === 'erase') {
        step--;
        cur = phrase.slice(0, step);
        el.textContent = cur;
        if (step <= 0) { mode = 'type'; idx = (idx + 1) % phrases.length; setTimeout(tick, 400); return; }
        setTimeout(tick, 24);
      }
    }
    tick();
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
          statusEl.textContent = '> error: 請檢查必填欄位 / please review the highlighted fields';
        }
      }
    });
  }

  /** Lang switcher — rewrites ?lang= on the URL and reloads. */
  function initLangSwitcher() {
    document.querySelectorAll('[data-lang]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var l = btn.getAttribute('data-lang');
        var u = new URL(window.location.href);
        u.searchParams.set('lang', l);
        window.location.href = u.toString();
      });
    });
  }

  /** Tabs on the products page. */
  function initTabs() {
    document.querySelectorAll('[data-tabs]').forEach(function (root) {
      var btns   = root.querySelectorAll('[data-tab]');
      var panels = document.querySelectorAll('[data-tab-panel]');
      btns.forEach(function (b) {
        b.addEventListener('click', function () {
          var key = b.getAttribute('data-tab');
          btns.forEach(function (x) { x.classList.toggle('is-active', x === b); });
          panels.forEach(function (p) {
            p.style.display = p.getAttribute('data-tab-panel') === key ? '' : 'none';
          });
        });
      });
    });
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
      form.querySelectorAll('[data-show-if]').forEach(function (b) {
        var rule = b.getAttribute('data-show-if').split('=');
        b.hidden = checkedValue(rule[0]) !== rule[1];
      });
    }

    function isValidContact(__v) {
      return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(__v) || /^\+?[0-9()\-\s#]{7,24}$/.test(__v);
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
      ['focus', 'click', 'input'].forEach(function (__type) { t.addEventListener(__type, pickOther); });
    });
    form.addEventListener('change', syncBranches);
    form.addEventListener('change', clearMark);
    form.addEventListener('input', clearMark);
    syncBranches();
    if (statusEl && /\b(ok|err)\b/.test(statusEl.className)) {
      statusEl.scrollIntoView({ block: 'center' });
    }

    form.addEventListener('submit', function (e) {
      var ok = true;
      form.querySelectorAll('.qf-field[data-required]').forEach(function (f) {
        f.classList.remove('is-invalid');
        if (f.closest('[hidden]')) return;
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
      if (!ok) {
        e.preventDefault();
        if (statusEl) {
          statusEl.className = 'qf-form-status err';
          statusEl.textContent = '> 還有必填的題目沒填，請看標紅的地方。';
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
      verify:    { img: 'bp_desk.jpg',      title: '最近一次，你確認 AI 產出的那一次' },
      explain:   { img: 'bp_chain.jpg',     title: '最近一次，有人問你「這是怎麼來的」' },
      inventory: { img: 'bp_mainframe.jpg', title: '最近一次，你需要弄清楚舊系統的那一次' },
      other:     { img: 'bp_desk.jpg',      title: '最近一次，你需要處理的那一件事' },
      none:      { img: 'bp_desk.jpg',      title: '如果都沒遇過，也請告訴我們' }
    };
    var partImg = form.querySelector('[data-sv-part-img]');
    var partTitle = form.querySelector('[data-sv-part-title]');
    var picks = document.querySelectorAll('[data-sv-pick]');

    function syncPicked() {
      var el = form.querySelector('input[name="situation"]:checked');
      var v = el ? el.value : '';
      picks.forEach(function (p) {
        var on = p.getAttribute('data-sv-pick') === v;
        if (p.hasAttribute('aria-pressed')) p.setAttribute('aria-pressed', on ? 'true' : 'false');
        var st = p.querySelector('.st');
        if (st) st.textContent = on ? '● 已選' : '選這個';
      });
      if (art[v] && partImg) {
        partImg.src = partImg.src.replace(/[^\/]+\.jpg$/, art[v].img);
        partTitle.textContent = art[v].title;
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
      if (__field.querySelector('input[type="radio"]')) {
        var picked = __field.querySelector('input[type="radio"]:checked');
        var other = __field.querySelector('[data-other-text]');
        return !!picked && !(other && picked.value === 'other' && !other.value.trim());
      }
      var input = __field.querySelector('input, textarea');
      return !!(input && input.value.trim());
    }
    function syncDone() {
      links.forEach(function (a, i) {
        var fs = required.filter(function (f) { return partOf(f) === i && !f.closest('[hidden]'); });
        a.classList.toggle('is-done', fs.length > 0 && fs.every(answered));
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
        out.textContent = n + ' 字' + (n < 20 ? ' · 幾句話就好' : '');
      }
      t.addEventListener('input', sync);
      sync();
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    initReveal();
    initTypewriter();
    initContactForm();
    initSurveyForm();
    initSurveyDots();
    initSurveyVisuals();
    initLangSwitcher();
    initTabs();
  });
})();
