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
          if (!f.querySelector('input[type="radio"]:checked')) { f.classList.add('is-invalid'); ok = false; }
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

  document.addEventListener('DOMContentLoaded', function () {
    initReveal();
    initTypewriter();
    initContactForm();
    initSurveyForm();
    initLangSwitcher();
    initTabs();
  });
})();
