/**
 * jQuery slicebox plugin
 * http://www.codrops.com/
 *
 * Copyright 2011, Pedro Botelho
 * Licensed under the MIT license.
 * http://www.opensource.org/licenses/mit-license.php
 *
 * Date: Thu Sep 5 2011
 */
(function( window, $, undefined ) {

	$.slicebox 					= function( options, element ) {
		this.$element 	= $( element );
		this._create( options );
	};
	
	$.slicebox.defaults 		= {
		orientation			: 'h',		// (v)ertical or (h)orizontal.
		direction			: 'next', 	// 'next' or previous 
		speed				: 1000,		// fallback speed. You might want to set a different speed for the fallback animation...
		fallbackEasing		: 'easeOutExpo' // fallback easing.
    };
	
	$.slicebox.prototype 		= {
		_create 			: function( options ) {
			var instance 			= this;
			
			instance.options 		= $.extend( true, {}, $.slicebox.defaults, options );
			
			instance._validate( instance.options );
			
			var $images				= instance.$element.children("div");
			
			// preload the images
			instance.box = new $.Box( instance.options, $images, instance.$element );
		},
		add					: function( $images, callback ) {
			var instance 			= this;
			this.box.addImages( $images, instance.options );
//			$images.remove();
			if ( callback ) callback.call( $images );
		},
		remove				: function( $images, callback ) {
			var instance 			= this;
			this.box.removeImages( $images );
			
			if ( callback ) callback.call( $images );
		},
		_validate			: function( options ) {
			if( options.orientation !== 'v' && options.orientation !== 'h' ) options.orientation = 'v';
		},
		destroy				: function( callback ) {
			this._destroy( callback );
		},
		_destroy 			: function( callback ) {
			this.element.unbind('.slicebox').removeData('slicebox');

			$(window).unbind('.slicebox');
			
			if ( callback ) callback.call();
		},
		option				: function( key, value ) {
			if ( $.isPlainObject( key ) ){
				this.options = $.extend( true, this.options, key );
			} 
		},
		navigate			: function( next ) {
			var instance			= this;
			
			this.box.navigate( next, instance.options );
		}
	};
	
	/*********************************** Box Fallback ********************************************************/
	
	$.Box 							= function( options, $images, $wrapper ) {
		this.size			= {					// assuming all images with same size
			width	: $images.width(),
			height	: $images.height()
		};
		this.animating		= false;
		this.$images		= $images;
		this.imagesCount	= this.$images.length;
		this.imageCurrent	= 0;
		this.orientation	= options.orientation;
		this.wrapper		= $wrapper;
		this.info			= false;
		
		this._createBox( options );
		this._configureImages( options );
	};
	
	$.Box.prototype 				= {
		_createBox 			: function( options ) {
			var boxStyle			= {
					'width'			: this.size.width + 'px',
					'height'		: this.size.height + 'px',
					'z-index'		: 10,
					'position'		: 'relative',
					'overflow'		: 'hidden'
				};
			
			this.$box				= this.wrapper.css( boxStyle );
		},
		_configureImages	: function( options ) {
			var instance			= this;
			
			instance.$images.each(function(i) {
				var $img	= $(this);
				
				if( i === 0) {
					$img.css({ left : '0px', top : '0px' });
				}	
				else {
					if( options.orientation === 'v')
						$img.css({ left : '0px', top : - instance.size.height + 'px' });
					else if( options.orientation === 'h')
						$img.css({ left : instance.size.width + 'px', top : '0px' });
				}	
					
			});
		},
		navigate			: function( next, options ) {
			var instance	= this;
			if( instance.animating ) return false;
	
			instance.animating			= true;
			
			var i = instance.$images.index( $('#' + next) );
			
			// should not be used, but just in case
			if ( i = -1 ) {
				this.addImages( $('#' + next), options );
				i = instance.$images.index( $('#' + next) );
			}
			
			this._slide( i, options );
		},
		_slide				: function( next, options, callback ) {
			var instance	= this,
				$current	= instance.$images.eq( instance.imageCurrent );
		
			instance.imageCurrent = next;
			
			var animParamOut	= {},
				animParamIn		= {};
			
			if( options.orientation === 'v') {
				animParamOut.top 	= ( options.direction === 'next' ) ? instance.size.height + 'px' : - instance.size.height + 'px';
				animParamIn.top 	= '0px';
			}
			else if( options.orientation === 'h') {
				animParamOut.left 	= ( options.direction === 'next' ) ? - instance.size.width + 'px' : instance.size.width + 'px';
				animParamIn.left 	= '0px';
			}
			
			$current.stop().animate(animParamOut, options.speed, options.fallbackEasing );
			
			var $next		= instance.$images.eq( instance.imageCurrent );
			
			if( options.direction === 'next' ) {
				if( options.orientation === 'v')
					$next.css( 'top', - instance.size.height + 'px' );
				else if( options.orientation === 'h')
					$next.css( 'left', instance.size.width + 'px' );
			}
			else {
				if( options.orientation === 'v')
					$next.css( 'top', instance.size.height + 'px' );
				else if( options.orientation === 'h')
					$next.css( 'left', - instance.size.width + 'px' );
			}
			
			$next.stop().animate(animParamIn, options.speed, options.fallbackEasing, function() {
				instance.animating			= false;
				if( callback ) callback.call();
			});
		},
		addImages			: function( $images, options, callback ) {
			var instance	= this;
			
			this.$images 		= this.$images.add( $images );
			this.imagesCount	= this.$images.length;
			
			$images.each(function(i) {
				var $img	= $(this);

				if( options.orientation === 'v')
					$img.css({ left : '0px', top : - instance.size.height + 'px' });
				else if( options.orientation === 'h')
					$img.css({ left : instance.size.width + 'px', top : '0px' });
			});
			
			if ( callback ) callback.call( $images );
		},
		removeImages		: function( $images, callback ) {		
			var instance	= this;

			instance.$images.each(function(i) {
				var $img	= $(this);
				
				var i = instance.$images.index($img);
				if (instance.imageCurrent > instance.$images.index($img)) {
					instance.imageCurrent--;
				}
			});

			this.$images 		= this.$images.not( $images );
			this.imagesCount	= this.$images.length;
			
			if ( callback ) callback.call( $images );
		}
	};
	
	var logError 				= function( message ) {
		if ( this.console ) {
			console.error( message );
		}
	};
	
	$.fn.slicebox 				= function( options ) {
		if ( typeof options === 'string' ) {
			var args = Array.prototype.slice.call( arguments, 1 );

			this.each(function() {
				var instance = $.data( this, 'slicebox' );
				if ( !instance ) {
					logError( "cannot call methods on slicebox prior to initialization; " +
					"attempted to call method '" + options + "'" );
					return;
				}
				if ( !$.isFunction( instance[options] ) || options.charAt(0) === "_" ) {
					logError( "no such method '" + options + "' for slicebox instance" );
					return;
				}
				instance[ options ].apply( instance, args );
			});
		} 
		else {
			this.each(function() {
				var instance = $.data( this, 'slicebox' );
				if ( !instance ) {
					$.data( this, 'slicebox', new $.slicebox( options, this ) );
				}
			});
		}
		return this;
	};
	
})( window, jQuery );