/*
mineplus/default.js - JavaScript Part for Minesweeper Plus

Minesweeper Plus - a Demo for JsWin
Designed by Morning Yellow, Version: 1.1, Last Modified: Sep. 2009
Copyright (C) 2004-2009 www.elephantbase.net

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

/* === Begin of Constants and Macros Part === */

// Win32 Constants
var IDI_APPICON		= 1;
var IMAGE_ICON		= 1;
var LR_SHARED		= 0x8000;
var WM_SETICON		= 0x80;
var ICON_SMALL		= 0;
var ICON_LARGE		= 1;

// Layout Constants
var APP_TITLE = "Minesweeper Plus";
var CLIENT_WIDTH	= 499;
var CLIENT_HEIGHT	= 320;
var BOX_SIZE		= 16;
var DIGIT_WIDTH		= 13;
var DIGIT_HEIGHT	= 23;
var COUNTER_LEFT	= 17;
var COUNTER_TOP		= 16;
var FACE_LEFT		= 240;
var FACE_TOP		= 16;
var FACE_SIZE		= 24;
var TIMER_LEFT		= 446;
var TIMER_TOP		= 16;
var BOARD_LEFT		= 12;
var BOARD_TOP		= 55;

var FACE_TOP_PRESS	= 0;
var FACE_TOP_WIN	= -FACE_SIZE;
var FACE_TOP_LOSS	= -2 * FACE_SIZE;
var FACE_TOP_HIT	= -3 * FACE_SIZE;
var FACE_TOP_RESET	= -4 * FACE_SIZE;

var DIGIT_TOP_MINUS	= 0;
var DIGIT_TOP_BLANK	= -DIGIT_HEIGHT;
var DIGIT_TOP_ZERO	= -11 * DIGIT_HEIGHT;

var BOX_TOP_UNHIT	= 0;
var BOX_TOP_FLAG	= -BOX_SIZE;
var BOX_TOP_MARK	= -2 * BOX_SIZE;
var BOX_TOP_HITMINE	= -3 * BOX_SIZE;
var BOX_TOP_WRONG	= -4 * BOX_SIZE;
var BOX_TOP_MINE	= -5 * BOX_SIZE;
var BOX_TOP_MARKPRESS	= -6 * BOX_SIZE;
var BOX_TOP_PRESS	= -15 * BOX_SIZE;

function DIGIT_TOP(digit) {
  return DIGIT_TOP_ZERO + DIGIT_HEIGHT * digit;
}

function BOX_TOP(mines) {
  return BOX_TOP_PRESS + BOX_SIZE * mines;
}

// Game Constants
var STATUS_NONE		= 0;
var STATUS_WIN		= 1;
var STATUS_LOSS		= 2;

var BOX_MINE		= 0xF;
var BOX_HIT		= 0x10;
var BOX_FLAG		= 0x20;

var MAX_MINES		= 99;
var BOARD_ROWS		= 16;
var BOARD_COLS		= 30;
var BOARD_SIZE		= (BOARD_ROWS + 2) * (BOARD_COLS + 2);
var MAX_HIT_BOXES	= BOARD_ROWS * BOARD_COLS - MAX_MINES;
var ADJACENT_BOXES	= 8;

var IN_BOARD;
var ADJACENT_DELTA = [BOARD_COLS + 3, BOARD_COLS + 2, BOARD_COLS + 1, 1,
    -1, -BOARD_COLS - 1, -BOARD_COLS - 2, -BOARD_COLS - 3];

var NO_AUTO_PLAY	= {};

/* === End of Constants and Macros Part === */

/* === Begin of Game Part === */

function initGameArrays(sq) {
  IN_BOARD = new Array(BOARD_SIZE);
  for (var sq = 0; sq < BOARD_SIZE; sq ++) {
    var row = Math.floor(sq / (BOARD_COLS + 2)) - 1;
    var col = sq % (BOARD_COLS + 2) - 1;
    IN_BOARD[sq] = (row >= 0 && row < BOARD_ROWS && col >= 0 && col < BOARD_COLS);
  }
}

var Game = {timer:null,box:new Array(BOARD_SIZE)};

function getMines(sq) {
  return Game.box[sq] & BOX_MINE;
}

function isHit(sq) {
  return (Game.box[sq] & BOX_HIT) != 0;
}

function isFlag(sq) {
  return (Game.box[sq] & BOX_FLAG) != 0;
}

function isUnhit(sq) {
  return (Game.box[sq] & (BOX_HIT | BOX_FLAG)) == 0;
}

function setBoxTop(sq, top) {
  Controls.divBox[sq].style.backgroundPosition = "0 " + top;
}

function setBoxImage(sq) {
  if (isHit(sq)) {
    var mines = getMines(sq);
    setBoxTop(sq, mines == BOX_MINE ? BOX_TOP_HITMINE : BOX_TOP(mines));
  } else if (isFlag(sq)) {
    setBoxTop(sq, BOX_TOP_FLAG);
  } else {
    setBoxTop(sq, BOX_TOP_UNHIT);
  }
}

function doAutoPlay() {
  var retry = true;
  while (retry) {
    retry = false;
    for (var sq = 0; sq < BOARD_SIZE; sq ++) {
      if (!(IN_BOARD[sq] && isHit(sq))) {
        continue;
      }
      var hits = 0, flags = 0, mines = getMines(sq);
      for (var i = 0; i < ADJACENT_BOXES; i ++) {
        var sq2 = sq + ADJACENT_DELTA[i];
        if (isHit(sq2)) {
          hits ++;
        } else if (isFlag(sq2)) {
          flags ++;
        }
      }
      if (hits + mines == ADJACENT_BOXES) {
        for (var i = 0; i < ADJACENT_BOXES; i ++) {
          var sq2 = sq + ADJACENT_DELTA[i];
          if (isUnhit(sq2)) {
            flagBox(sq2, false);
            retry = true;
          }
        }
      } else if (flags == mines) {
        for (var i = 0; i < ADJACENT_BOXES; i ++) {
          var sq2 = sq + ADJACENT_DELTA[i];
          if (isUnhit(sq2)) {
            hitBox(sq2, false);
            if (Game.status != STATUS_NONE) {
              return;
            }
            retry = true;
          }
        }
      }
    }
  }
}

function hitBox(sqHit, autoPlay) {
  if (!isUnhit(sqHit)) {
    return;
  }

  // Lay Mines after First Hit
  if (Game.hitBoxes == 0) {
    var mines = 0;
    while (mines < MAX_MINES) {
      var row = Math.floor(Math.random() * BOARD_ROWS);
      var col = Math.floor(Math.random() * BOARD_COLS);
      var sq = (row + 1) * (BOARD_COLS + 2) + col + 1;
      if (sq != sqHit && Game.box[sq] == 0) {
        Game.box[sq] = BOX_MINE;
        mines ++;
      }
    }
    Game.timer = setInterval(function() {
      Game.seconds ++;
      setDigitImages(Controls.divTimer, Game.seconds);
      if (Game.seconds == 1000) {
        clearInterval(Game.timer);
      }
    }, 1000);
  }

  if (Game.box[sqHit] == BOX_MINE) {
    // Loss
    for (var sq = 0; sq < BOARD_SIZE; sq ++) {
      if (IN_BOARD[sq]) {
        if (isFlag(sq)) {
          if (getMines(sq) != BOX_MINE) {
            setBoxTop(sq, BOX_TOP_WRONG);
          }
        } else if (getMines(sq) == BOX_MINE) {
          setBoxTop(sq, BOX_TOP_MINE);
        }
      }
    }
    Game.box[sqHit] = BOX_HIT + BOX_MINE;
    setBoxTop(sqHit, BOX_TOP_HITMINE);
    Game.status = STATUS_LOSS;
    setFaceImage();
    clearInterval(Game.timer);
  } else {
    // Hit
    var mines = 0;
    for (var i = 0; i < ADJACENT_BOXES; i ++) {
      if (getMines(sqHit + ADJACENT_DELTA[i]) == BOX_MINE) {
        mines ++;
      }
    }
    Game.box[sqHit] = BOX_HIT + mines;
    setBoxImage(sqHit);
    Game.hitBoxes ++;
    if (Game.hitBoxes == MAX_HIT_BOXES) {
      // Win
      for (var sq = 0; sq < BOARD_SIZE; sq ++) {
        if (IN_BOARD[sq] && getMines(sq) == BOX_MINE) {
          setBoxTop(sq, BOX_TOP_FLAG);
        }
      }
      Game.mines = 0;
      setDigitImages(Controls.divCounter, Game.mines);
      Game.status = STATUS_WIN;
      setFaceImage();
      clearInterval(Game.timer);
    } else if (autoPlay) {
      doAutoPlay();
    }
  }
}

function flagBox(sq, autoPlay) {
  if (!isHit(sq)) {
    Game.box[sq] ^= BOX_FLAG;
    setBoxImage(sq);
    Game.mines += (isFlag(sq) ? -1 : 1);
    setDigitImages(Controls.divCounter, Game.mines);
    if (autoPlay && isFlag(sq)) {
      doAutoPlay();
    }
  }
}

function resetGame() {
  Game.status = STATUS_NONE;
  setFaceImage();

  Game.mines = MAX_MINES;
  setDigitImages(Controls.divCounter, Game.mines);

  Game.seconds = 0;
  setDigitImages(Controls.divTimer, Game.seconds);
  if (Game.timer != null) {
    clearInterval(Game.timer);
  }

  for (var sq = 0; sq < BOARD_SIZE; sq ++) {
    if (IN_BOARD[sq]) {
      Game.box[sq] = 0;
      setBoxTop(sq, BOX_TOP_UNHIT);
    } else {
      Game.box[sq] = BOX_HIT;
    }
  }

  Game.hitBoxes = 0;
}

/* === End of Game Part === */

/* === Begin of UI Part === */

function setDivTop(div, top) {
  div.style.backgroundPosition = "0 " + top;
}

function setFaceTop(top) {
  setDivTop(Controls.divFace, top);
}

function setFaceImage() {
  setFaceTop(Game.status == STATUS_WIN ? FACE_TOP_WIN : Game.status == STATUS_LOSS ? FACE_TOP_LOSS : FACE_TOP_RESET);
}

function setDigitImages(arrDiv, digit) {
  if (digit > 999 || digit < -99) {
    for (var i = 0; i < 3; i ++) {
      setDivTop(arrDiv[i], DIGIT_TOP_MINUS);
    }
  } else {
    absDigit = Math.abs(digit);
    for (var i = 0; i < 3; i ++) {
      setDivTop(arrDiv[2 - i], DIGIT_TOP(absDigit % 10));
      absDigit = Math.floor(absDigit / 10);
    }
    if (digit < 0) {
      setDivTop(arrDiv[0], DIGIT_TOP_MINUS);
    }
  }
}

var Controls = {};

function appendDiv(x, y, width, height, image) {
  var div = document.createElement("div");
  div.style.left = x;
  div.style.top = y;
  div.style.width = width;
  div.style.height = height;
  div.style.backgroundImage = "url(" + image + ".gif)";
  document.body.appendChild(div);
  return div;
}

function appendDigit(x, y) {
  return appendDiv(x, y, DIGIT_WIDTH, DIGIT_HEIGHT, "digit");
}

function appendBox(x, y) {
  return appendDiv(x, y, BOX_SIZE, BOX_SIZE, "box");
}

function appendFace(x, y) {
  return appendDiv(x, y, FACE_SIZE, FACE_SIZE, "face");
}

function createCallback(callback, args) {
  return function() {
    callback(args, arguments[0] || event);
  }
}

function initImageControls() {
  Controls.divCounter = new Array(3);
  for (var i = 0; i < 3; i ++) {
    Controls.divCounter[i] = appendDigit(COUNTER_LEFT + DIGIT_WIDTH * i, COUNTER_TOP);
  }
  Controls.divFace = appendFace(FACE_LEFT, FACE_TOP);
  Controls.divFace.onclick = resetGame;
  Controls.divFace.onmousedown = function() {
    var e = arguments[0] || event;
    if (e.button != 2) {
      setFaceTop(FACE_TOP_PRESS);
    }
  };
  Controls.divFace.onmouseup = Controls.divFace.onmouseout = function() {
    setFaceImage();
  };
  Controls.divTimer = new Array(3);
  for (var i = 0; i < 3; i ++) {
    Controls.divTimer[i] = appendDigit(TIMER_LEFT + DIGIT_WIDTH * i, TIMER_TOP);
  }
  Controls.divBox = new Array(BOARD_SIZE);
  for (var row = 0; row < BOARD_ROWS; row ++) {
    for (var col = 0; col < BOARD_COLS; col ++) {
      var div = appendBox(BOARD_LEFT + BOX_SIZE * col, BOARD_TOP + BOX_SIZE * row);
      var sq = (row + 1) * (BOARD_COLS + 2) + col + 1;
      div.onclick = createCallback(function(args_sq) {
        if (Game.status == STATUS_NONE) {
          hitBox(args_sq, true);
        }
      }, sq);
      div.onmousedown = createCallback(function(args_sq, e) {
        if (Game.status == STATUS_NONE) {
          if (e.button == 2) {
            flagBox(args_sq, true);
          } else if (isUnhit(args_sq)) {
            setBoxTop(args_sq, BOX_TOP_PRESS);
            setFaceTop(FACE_TOP_HIT);
          }
        }
      }, sq);
      div.onmouseup = div.onmouseout = createCallback(function(args_sq) {
        if (Game.status == STATUS_NONE && isUnhit(args_sq)) {
          setBoxTop(args_sq, BOX_TOP_UNHIT);
          setFaceTop(FACE_TOP_RESET);
        }
      }, sq);
      Controls.divBox[sq] = div;
    }
  }
}

/* === End of UI Part === */

function main() {
  if (typeof VB == "object") {
    VB.App.Title = VB.Caption = APP_TITLE;

    // Application Icon
    var fnLoadImage = VB.GetProcAddress(JS.win32.modUser, "LoadImageA");
    var fnSendMessage = VB.GetProcAddress(JS.win32.modUser, "SendMessageA");
    var hIconSmall = JS.callProc(fnLoadImage, VB.App.hInstance, IDI_APPICON, IMAGE_ICON, 16, 16, LR_SHARED);
    var hIconLarge = JS.callProc(fnLoadImage, VB.App.hInstance, IDI_APPICON, IMAGE_ICON, 32, 32, LR_SHARED);
    JS.callProc(fnSendMessage, VB.hWnd, WM_SETICON, ICON_SMALL, hIconSmall);
    JS.callProc(fnSendMessage, VB.hWnd, WM_SETICON, ICON_LARGE, hIconLarge);

    JS.show(STYLE_FIXED, CLIENT_WIDTH, CLIENT_HEIGHT);
  }

  initGameArrays();
  initImageControls();
  resetGame();
}