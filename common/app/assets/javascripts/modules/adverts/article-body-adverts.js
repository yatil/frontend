/*global guardian */
define([
    'common/$',
    'common/common',
    'common/modules/component',
    'bonzo',
    'bean',
    'lodash/objects/assign',
    'common/utils/detect'
], function (
    $,
    common,
    Component,
    bonzo,
    bean,
    extend,
    detect
) {

    function ArticleBodyAdverts(config) {
        this.context = document;
        this.config = extend(this.config, config);
    }

    Component.define(ArticleBodyAdverts);

    ArticleBodyAdverts.prototype.config = {
        inlineAdLimit: null,
        nthParagraph: 7,
        inlineAdTemplate: '<div class="ad-slot ad-slot--inline" data-base="%slot%" data-median="%slot%"><div class="ad-container"></div></div>',
        mpuAdTemplate: '<div class="ad-slot ad-slot--mpu-banner-ad" data-link-name="ad slot mpu-banner-ad" data-base="%slot%" data-median="%slot%"><div class="ad-container"></div></div>'
    };

    // inserts a few inline advert slots in to the page
    ArticleBodyAdverts.prototype.createInlineAdSlots = function(id) {
        var paragraphSelector = 'p:nth-of-type('+ this.config.nthParagraph +'n)',
            limit             = this.config.inlineAdLimit,
            template          = this.config.inlineAdTemplate,
            article           = document.getElementsByClassName('js-article__container')[0];

        $(paragraphSelector, article).each(function(el, i) {
            var $el = $(el),
                cls = (i % 2 === 0) ? 'is-odd' : 'is-even';

            /*
             - Checks if limit is set and if so, checks it hasn't been exceeded
             - Checks is the $target element exists. If not, then you are at the end of the article
             - Checks if the text length is below 120 characters - helps prevent against empty paragraphs
               and paragraphs being used instead of order/unordered lists
             */
            if(limit !== null && limit < (i + 1) || $el.next()[0] === undefined || $el.text().length < 120) {
                return false;
            }

            bonzo(bonzo.create(template.replace(/%slot%/g, id))).addClass(cls).insertBefore($el);
        });
    };

    ArticleBodyAdverts.prototype.createMpuAdSlot = function(id) {
        var template = this.config.mpuAdTemplate;

        $('.js-mpu-ad-slot .social-wrapper').after(bonzo.create(template.replace(/%slot%/g, id))[0]);
    };

    ArticleBodyAdverts.prototype.destroy = function() {
        $('.ad-slot--inline, .ad-slot--mpu-banner-ad').remove();
    };

    ArticleBodyAdverts.prototype.reload = function() {
        this.destroy();
        this.init();
    };

    ArticleBodyAdverts.prototype.init = function() {
        var breakpoint = detect.getBreakpoint();

        if((/wide|desktop/).test(breakpoint)) {
            this.createInlineAdSlots('Middle1');
            this.createMpuAdSlot('Middle');
        }

        if((/mobile/).test(breakpoint)) {
            this.createInlineAdSlots('x49');
        }
    };

    return ArticleBodyAdverts;

});