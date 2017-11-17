(ns lumo-quiz-game.main
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [macchiato.fs :as fs]
            [lumo.io :as io]
            [chalk-animation :as animate]
            [close-enough :as close-enough]))

(def answer-chan (chan 1))

(def start-chan (chan 1))

(defn compare-answer [actual given]
  (.compare (close-enough) actual given))

(defn generate-rainbow-text [text]
  (.rainbow animate text))

(defn generate-glitch-text [text]
  (.glitch animate text))

(defn generate-neon-text [text]
  (.neon animate text))

(defn generate-pulse-text [text]
  (.pulse animate text))

(defn generate-radar-text [text]
  (.radar animate text))

(def questions-db
  (-> "resources/questions.json"
      io/resource
      io/slurp
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn random-question []
  (let [question-cnt (count questions-db)
        random-number (int (rand question-cnt))]
    (nth questions-db random-number)))

(def readline (js/require "readline"))

(def rl (.createInterface readline
                          (clj->js {:input  (.-stdin  js/process)
                                    :output (.-stdout js/process)})))

(go (<! start-chan)
    (go-loop []
      (let [{category :category
             question :question
             answer :answer} (random-question)
            anim (.rainbow animate (str "Category: " category))]
        (<! (timeout 1000))
        (.stop anim)
        (println (str "Question is: " question))
        (.prompt rl)
        (let [given-answer (<! answer-chan)]
          (generate-pulse-text (str "You guessed: " given-answer))
          (<! (timeout 2000))
          (if (compare-answer answer given-answer)
            (generate-neon-text (str given-answer " was correct!"))
            (generate-radar-text (str "Wrong :( :( correct answer is: " answer)))
          (<! (timeout 2000))
          (recur)))))


(defn -main []
  (go (generate-glitch-text "#$#\"$#\"!!!!Welcom to LUMO QUIZ!!!!\"#$#\"$#")
      (<! (timeout 2000))
      (>! start-chan true)
      (doto rl
        (.setPrompt "GUESS>> ")
        (.on "line"
             (fn [line]
               (go (>! answer-chan line))))
        (.prompt))))
