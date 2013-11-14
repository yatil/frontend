define([
    'models/common',
    'models/humanizedTimeSpan',
    'models/authedAjax',
    'knockout'
],
function (
    common,
    humanizedTimeSpan,
    authedAjax,
    ko
){
    var absUrlHost = 'http://m.guardian.co.uk/';

    function Article(opts, collection) {
        var opts = opts || {};

        this.collection = collection;

        this.props = common.util.asObservableProps([
            'id',
            'webPublicationDate']);

        this.fields = common.util.asObservableProps([
            'headline',
            'thumbnail',
            'trailText',
            'shortId']);

        this.meta = common.util.asObservableProps([
            'headline',
            'group']);

        this.state = common.util.asObservableProps([
            'underDrag',
            'isEditing',
            'shares',
            'comments',
            'totalHits',
            'pageViewsSeries']);

        // Computeds
        this.humanDate = ko.computed(function(){
            return this.props.webPublicationDate() ? humanizedTimeSpan(this.props.webPublicationDate()) : '';
        }, this);
        
        this.totalHitsFormatted = ko.computed(function(){
            return common.util.numberWithCommas(this.state.totalHits());
        }, this);

        this.headlineInput = ko.computed({
            read: function() {
                return this.meta.headline() || this.fields.headline();
            },
            write: function(value) {
                this.meta.headline(value);
            },
            owner: this
        });

        this.provisionalHeadline = null;

        this.populate(opts);
    };

    Article.prototype.populate = function(opts) {
        common.util.populateObservables(this.props, opts);
        common.util.populateObservables(this.meta, opts.meta);
        common.util.populateObservables(this.fields, opts.fields);
    };

    Article.prototype.startEdit = function() {
        this.provisionalHeadline = this.meta.headline();
        this.state.isEditing(true);
    };

    Article.prototype.saveEdit = function() {
        if(this.meta.headline()) {
            this.save();
        };
        this.state.isEditing(false);
    };

    Article.prototype.cancelEdit = function() {
        this.meta.headline(this.provisionalHeadline);
        this.state.isEditing(false);
    };

    Article.prototype.revertEdit = function() {
        this.meta.headline(undefined);
        this.state.isEditing(false);
        this.save();
    };

    Article.prototype.getMeta = function() {
        var self = this;

        return _.chain(this.meta)
            .pairs()
            // is the meta property a not a whitespace-only string ?
            .filter(function(p){ return !_.isUndefined(p[1]()) && ("" + p[1]()).replace(/\s*/g, '').length > 0; })
            // does it actually differ from the props value (if any) that it's overwriting ?
            .filter(function(p){ return  _.isUndefined(self.props[p[0]]) || self.props[p[0]]() !== p[1](); })
            .map(function(p){ return [p[0], p[1]()]; })
            .object()
            .value();
    };

    Article.prototype.save = function() {
        if (!this.collection) { return; }

        authedAjax.updateCollection(
            'post',
            this.collection,
            {
                item:     this.props.id(),
                position: this.props.id(),
                itemMeta: this.getMeta(),
                live:     common.state.liveMode(),
                draft:   !common.state.liveMode(),
            }
        );
        this.collection.state.loadIsPending(true)
    };

    return Article;
});
