package com.example.nfcmuseumguide.data

import com.example.nfcmuseumguide.model.Exhibit

fun defaultMuseumCatalog(): List<Exhibit> = listOf(
    Exhibit(
        id = "star-map-001",
        titleRu = "Звёздная карта Новгорода",
        titleEn = "Novgorod Star Map",
        subtitleRu = "Медная пластина с небесными координатами, XII век",
        subtitleEn = "A copper sky plate with celestial coordinates, 12th century",
        descriptionRu = "Экспонат показывает, как средневековые мастера представляли движение звёзд. NFC-метка у витрины открывает карточку, аудиогид и маршрут.",
        descriptionEn = "The artifact shows how medieval makers imagined the motion of the stars. The NFC tag near the showcase opens the card, audio guide and route.",
        zone = "Зал Космоса",
        floor = 2,
        century = "XII век",
        category = "Астрономия",
        routeOrder = 1,
        tags = listOf("звёзды", "навигация", "медь"),
        facts = listOf("На пластине 47 точек-звёзд", "Маршрут начинается с этого объекта", "Метка NFC привязана к витрине 1")
    ),

    Exhibit(
        id = "mask-echo-014",
        titleRu = "Маска Эха",
        titleEn = "The Echo Mask",
        subtitleRu = "Ритуальная маска с акустическими резонаторами",
        subtitleEn = "A ritual mask with acoustic resonators",
        descriptionRu = "Внутри маски спрятаны полости, усиливающие голос. В гиде есть аудио-реконструкция и подробное описание конструкции.",
        descriptionEn = "Hidden cavities inside the mask amplify the voice. The guide includes audio reconstruction and a detailed explanation.",
        zone = "Зал Ритуалов",
        floor = 1,
        century = "IX век",
        category = "Антропология",
        routeOrder = 2,
        tags = listOf("ритуал", "звук", "маска"),
        facts = listOf("Резонаторы меняют тембр голоса", "Экспонат лучше слушать в наушниках", "Маркер NFC привязан к витрине 14")
    ),
    Exhibit(
        id = "glass-garden-029",
        titleRu = "Стеклянный сад",
        titleEn = "Glass Garden",
        subtitleRu = "Инсталляция из фрагментов витражей и линз",
        subtitleEn = "An installation made of stained glass fragments and lenses",
        descriptionRu = "Работа создана как интерактивный объект: при сканировании NFC посетитель получает персональный маршрут по цветам коллекции.",
        descriptionEn = "This work is interactive: after scanning NFC, visitors receive a personal route through the colors of the collection.",
        zone = "Галерея Света",
        floor = 3,
        century = "XXI век",
        category = "Современное искусство",
        routeOrder = 3,
        tags = listOf("свет", "линзы", "цвет"),
        facts = listOf("Каждый фрагмент имеет свой оттенок", "В приложении есть цветовой маршрут", "Фото можно заменить в редакторе для пользовательских экспонатов")
    ),
    Exhibit(
        id = "codex-aurora-052",
        titleRu = "Кодекс Авроры",
        titleEn = "Codex Aurora",
        subtitleRu = "Манускрипт с невидимыми чернилами",
        subtitleEn = "A manuscript with invisible ink",
        descriptionRu = "На страницах есть слой текста, который виден только под определённым светом. Гид объясняет, как работали такие чернила.",
        descriptionEn = "The pages contain a text layer visible only under special light. The guide explains how such ink worked.",
        zone = "Архив",
        floor = 1,
        century = "XV век",
        category = "Рукописи",
        routeOrder = 4,
        tags = listOf("архив", "кодекс", "чернила"),
        facts = listOf("Скрытый слой содержит пометки переписчика", "Экспонат входит в ночной маршрут", "NFC-метка хранит только ID экспоната")
    ),
    Exhibit(
        id = "clock-whale-077",
        titleRu = "Часы Кита",
        titleEn = "Whale Clock",
        subtitleRu = "Механизм, синхронизированный с приливами",
        subtitleEn = "A mechanism synchronized with tides",
        descriptionRu = "Эти часы не показывали время в часах. Они отображали приливы, фазы луны и сезонные переходы для портового города.",
        descriptionEn = "This clock did not show hours. It displayed tides, moon phases and seasonal transitions for a port city.",
        zone = "Морской зал",
        floor = 2,
        century = "XVIII век",
        category = "Механика",
        routeOrder = 5,
        tags = listOf("море", "механизм", "луна"),
        facts = listOf("Внутри 128 зубчатых элементов", "В маршруте это финальная точка", "Открытия через NFC попадают в статистику")
    )
)
