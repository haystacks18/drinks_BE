package com.goormfj.hanzan.drinkTest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Controller
public class DrinkTypeController {

    private static final Map<String, Consumer<int[]>> answerActions = new HashMap<>();

    static {
        // 질문 1에 대한 응답 로직
        answerActions.put("1A", scores -> scores[4]++);
        answerActions.put("1B", scores -> { scores[1]++; scores[5]++; });
        answerActions.put("1C", scores -> { scores[2]++; scores[6]++; });
        answerActions.put("1D", scores -> { scores[3]++; scores[7]++; });

        // 질문 2에 대한 응답 로직
        answerActions.put("2A", scores -> { scores[0]++; scores[4]++; });
        answerActions.put("2B", scores -> { scores[2]++; scores[6]++; });
        answerActions.put("2C", scores -> { scores[1]++; scores[5]++; });
        answerActions.put("2D", scores -> { scores[3]++; scores[7]++; });

        // 질문 3에 대한 응답 로직
        answerActions.put("3A", scores -> { scores[0]++; scores[4]++; });
        answerActions.put("3B", scores -> { scores[3]++; scores[5]++; });
        answerActions.put("3C", scores -> { scores[2]++; scores[6]++; });
        answerActions.put("3D", scores -> scores[7]++);

        // 질문 4에 대한 응답 로직
        answerActions.put("4A", scores -> scores[1]++);
        answerActions.put("4B", scores -> { scores[0]++; scores[4]++; scores[5]++; });
        answerActions.put("4C", scores -> scores[7]++);
        answerActions.put("4D", scores -> { scores[2]++; scores[6]++; });
    }


    @GetMapping("/drink-test")
    public String showTest() {
        return null; //html 아니고 원래대로 질문이 들어와야함...!!!!!
    }

    @PostMapping("/result")
    public String drinkTypeResult(@RequestParam String answer1, @RequestParam String answer2,
                                  @RequestParam String answer3, @RequestParam String answer4,
                                  Model model) {
        int[] scores = new int[8];
        executeAction("1" + answer1, scores);
        executeAction("2" + answer2, scores);
        executeAction("3" + answer3, scores);
        executeAction("4" + answer4, scores);


        String drinkType = determineDrinkType(scores);
        model.addAttribute("drinkType", drinkType);
        return "result";
    }

    private void executeAction(String key, int[] scores) {
        answerActions.getOrDefault(key, s -> {}).accept(scores);
    }

    private String determineDrinkType(int[] scores) {
        // 로직을 통해 가장 높은 점수를 가진 타입 찾기
        int maxIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[maxIndex]) {
                maxIndex = i;
            }
        }
        // 타입에 따른 설명 반환
        return getTypeDescription(maxIndex);
    }

    private String getTypeDescription(int typeIndex) {
        switch (typeIndex) {
            case 0: return "모두에게 사랑받는 아이스박스";
            case 1: return "치고 나가는 스포츠카";
            case 2: return "궁금한게 많은 명탐정!";
            case 3: return "인생은 나홀로 철학자";
            case 4: return "흥미진진한 진행MC";
            case 5: return "다 챙겨주는 츤데레";
            case 6: return "자유로운 영혼 로커";
            case 7: return "어느모임이든 사수하는 파티 킬러";
            default: return "알 수 없는 타입";
        }
    }
}
