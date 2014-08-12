(ns civs.acceptance-test
  (:require [clojure.test :refer :all]
            [civs.core :refer :all]
            [civs.model :refer :all]
            [civs.logic :refer :all]
            [civs.logic.basic :refer :all]
            [civs.logic.demographics :refer :all]
            [civs.logic.tribe-choices :refer :all]))

; Here we just check nothing really weird happens and some basic
; expectations are met in a few random cases

(def w77 (load-world "examples-worlds/seed_77.world"))

(def verbose-acceptance-tests true)

(defn mock-crand-int []
  (let [my-r (Random. 1)]
    (fn [n] (.nextInt my-r n))))

(defn mock-crand-float []
  (let [my-r (Random. 1)]
    (fn [] (.nextFloat my-r))))

(defn mock-land-biome [desired-biome]
  (fn [world pos] (if (isLand world pos) desired-biome com.github.lands.Biome/OCEAN)))

(deftest test-not-everyone-dies-immediately
  (with-redefs )
  (let [g0 (generate-game w77 10)
        g (reduce (fn [g _] (turn g)) g0 (repeat 10 :_))
        crand-int   mock-crand-int
        crand-float mock-crand-float]
    (is (> (game-total-pop g) 0))))

(deftest test-population-do-not-expand-too-much-too-fast
  (let [g0 (generate-game w77 10)
        start-pop (game-total-pop g0)
        g (reduce (fn [g _] (turn g)) g0 (repeat 10 :_))
        crand-int  mock-crand-int
        rand-float mock-crand-float]
    (is (< (game-total-pop g) (* start-pop 2)))))

; We do not want to drop too drammatically and it should not increase too much

(defn check-biome biome min-factor max-factor ntribes nturns
  (with-redefs [
                 biome-at (mock-land-biome biome)
                 crand-int   mock-crand-int
                 crand-float mock-crand-float]
    (let [g0 (generate-game w77 ntribes)
          start-pop (game-total-pop g0)
          g (reduce (fn [g _] (turn g)) g0 (repeat nturns :_))
          final-pop (game-total-pop g)]
      (when verbose-acceptance-tests
        (println (.toString biome) " scenario: initial population " start-pop)
        (println (.toString biome) " scenario: final population "   final-pop))
      (is (> final-pop (* start-pop min-factor)))
      (is (< final-pop (* start-pop max-factor))))))

(deftest test-population-in-a-sand-desert
  (check-biome com.github.lands.Biome/SAND_DESERT 0.5 1.3 30 10))

; We do not want to drop too drammatically and it should not increase too much
(deftest test-population-in-a-grassland
  (check-biome com.github.lands.Biome/GRASSLAND 0.9 1.7 30 10))

; We do not want to drop too drammatically and it should not increase too much
(deftest test-population-in-a-savanna
  (check-biome com.github.lands.Biome/SAVANNA 0.9 1.5 30 10))

; We do not want to drop too drammatically and it should not increase too much
(deftest test-population-in-a-forest
  (check-biome com.github.lands.Biome/FOREST 0.9 1.6 30 10))

; We do not want to drop too drammatically and it should not increase too much
(deftest test-population-in-a-jungle
  (check-biome com.github.lands.Biome/JUNGLE 0.9 1.7 30 10))