define([
    'common/utils/detect'
], function(
    detect
    ) {

    return function() {

        this.id = 'InBodyLinksNewTab';
        this.expiry = '2014-02-28';
        this.audience = 1;
        this.audienceOffset = 1;
        this.description = 'Test whether opening in body links in a new tab increases page views per session';
        this.canRun = function(config) {
            return detect.getBreakpoint() !== 'mobile' &&
            config.page.contentType === 'Article' &&
            config.page.inBodyExternalLinkCount > 0;
        };
        this.variants = [
            {
                id: 'control',
                test: function () {
                   return true;
                }
            },
            {
                id: 'NewTab',
                test: function(context) {
                    context.querySelector('.article-body').addEventListener('click', function(e) {
                        if (e.target && e.target.nodeName === 'A') {
                            var href = e.target.href;
                            if (!href.match(document.location.host)) {
                                e.target.setAttribute('target', '_blank');
                            }
                        }
                    });
                }
            }
        ];
    };
});
