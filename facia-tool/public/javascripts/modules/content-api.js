/* global _: true */
define([
    'modules/authed-ajax',
    'modules/vars',
    'modules/cache'
],
function (
    authedAjax,
    vars,
    cache
){
    function validateItem (item) {
        var data = cache.get('contentApi', item.id),
            defer = $.Deferred();

        if(data) {
            populate(data, item);
            defer.resolve();
        } else {
            fetchData([item.id])
            .done(function(result){
                if (result.length === 1) {
                    result = result[0];
                    cache.put('contentApi', result.id, result);
                    populate(result, item);
                    defer.resolve();
                } else {
                    defer.reject();
                }
            }).fail(function(){
                defer.reject();
            });
        }
        return defer.promise();
    }

    function decorateItems (items) {
        var ids = [];

        items.forEach(function(item){
            var data = cache.get('contentApi', item.id);
            if(data) {
                populate(data, item);
            } else {
                ids.push(item.id);
            }
        });

        fetchData(ids)
        .done(function(results){
            results.forEach(function(article){
                cache.put('contentApi', article.id, article);
                _.filter(items, function(item){
                    return item.id === article.id;
                }).forEach(function(item){
                    populate(article, item);
                });
            });

            _.each(items, function(item){
                item.state.isEmpty(!item.state.isLoaded());
            });
        });
    }

    function populate(opts, article) {
        article.populate(opts, true);
    }

    function fetchData(ids) {
        var apiUrl,
            defer = $.Deferred();

        if (ids.length) {
            apiUrl = vars.CONST.apiSearchBase + "/search?page-size=50&format=json&show-fields=all";
            apiUrl += "&ids=" + ids.map(function(id){
                return encodeURIComponent(id);
            }).join(',');

            authedAjax.request({
                url: apiUrl
            }).always(function(resp) {
                if (resp.response && _.isArray(resp.response.results)) {
                    defer.resolve(resp.response.results);
                } else {
                    defer.reject();
                }
            });
        }
        return defer;
    }

    return {
        decorateItems: decorateItems,
        validateItem:  validateItem
    };

});
