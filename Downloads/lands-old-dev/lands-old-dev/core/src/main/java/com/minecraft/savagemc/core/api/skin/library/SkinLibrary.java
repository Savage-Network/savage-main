package com.minecraft.savagemc.core.api.skin.library;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.api.skin.objects.SkinCategory;
import com.minecraft.savagemc.core.api.skin.objects.SkinCollection;
import com.minecraft.savagemc.core.api.skin.objects.SkinGender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum SkinLibrary {

    /* Character Skins */
    CHARACTER_SCORPION(SkinCategory.CHARACTER, SkinCollection.MORTAL_KOMBAT, SkinGender.MAN, Skin.library("Scorpion",
            "ewogICJ0aW1lc3RhbXAiIDogMTYyMDA5NjA2NTE5NCwKICAicHJvZmlsZUlkIiA6ICI0ZjU2ZTg2ODk2OGU0ZWEwYmNjM2M2NzRlNzQ3ODdjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ3ZTI4NDBiMzk5Y2ZlOTQ2MzJmMDFkZDQyNTExY2I4OWU1YmI4ODU4YTkyYjQ3OWE0YWY5N2Y0ODc1NjhlM2MiCiAgICB9CiAgfQp9",
            "nlSX0KnVQvsuytsEyz1VwQBGAlAkhDsQcX8+H0QZlVKOBDHplhNOzt9v6djW1v0/Q+9tUqhq+wrunIfwdrC5ZUy5YEc3ExgUaEEdrhz3UhJdFD1AHEnfzxt/JgcLUOyNK5EPTiKshCOZCQeRY1nSrcVTd4nsV6nNsmK3CNGC85Cu1cO3oi+yK8ScMJnD9vneAcD09NJXulBbpxprAAD8x1a61WDTKIajPee0Vdq9CD2XjZs1iXjS/exxxSNEztfKbuCGN5Dgvv2wD14ZICHJepFcf+LODumJPSsy9ZSRmyhGs1dDopqFhdmVLIg7Ggw/iQZkpPbngs8PQUZhfODksH1bAbMV+EJRSzbD0ZWjY/rdb40t90DmnLzy3JNdelg92dlQhrlHI7KK8Z4IiY0VeHn63pKmcZslFDQeuZ0R8AzA/UlQ32TiU8cJ2mxJ2haqvaOAjr7mKCNA9QT3QYQnFZYJYL8bHo+27FnqFmODPH8Sy9Y8DHuIn2t/YBMX904LUKJNaU3aMCP/wF/1BMjP+s5enMxkDNTahOqGxlig9kHfGg1Dmou/J/TpV4XUOE8sOf1y27qD9FQi+IpvFf1l/CkFbHQ5iXkBvjbxUv3cx/SyM9uNSHcT+PmZzzZpVWZkRiYfhKMAS/hIEp7oTxV3MEQHq/99NVToljxxE2Yob9E="),
            1726006523265L),
    CHARACTER_RYU(SkinCategory.CHARACTER, SkinCollection.STREET_FIGHTER, SkinGender.MAN, Skin.library("Ryu",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxMDQyMjY1ODc3NiwKICAicHJvZmlsZUlkIiA6ICI5YzM5OTdhMjVjNWY0NmY0OWZlMWFhY2RlZjRiMmMwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLaWxsZXJmcmVkZHk4OTQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQyMzdkMDUxYTIxOWQ1YmI1OTdkODhhZDVlNDY3OWVjYmZmYWYyODVhNTUxMGJjZGI4ZTFjOGVlOGNkOTFlMyIKICAgIH0KICB9Cn0=",
            "DI6Z0tI8nRretBVCem1jKf8yHYZBDutjoRanuJO+C/KSFJjDkIYfCLso4HNoxJ5InrMPIcalbVFHRIRGi1e2ivrH4xWS6z/OMMk4wZAuFadKh0aQrfV3M42uErmZYpuT1z8fRxnv+OMJULbuyMjQoL4/IwIcqcTkDc0QCEhLrXPb4rkvrTFxsYsxc8FepLEp0+iVssuZa/FiUGq21EhGDtsmdd5uu3kQqF4wGjAH+yS3/VSZ5RA0AHiRqxYt77gFfEmGu/JrYapIKRvhWOqy8j2nOFiQnb4O5PFS0vIYDX9Maok5a3+0SgqKQwWItbL1P1+hLyC+l24wWa0QLJee/6JWlHArEMrGtvImvSh3ZPex4Vv9NsW+n+FslUb16/vttIR8uW5PLcdktS0NghZa6Sb4P//lJf3bwGWkFDmr+pv913QwQOnlibWVX28WU+upBGdDYawa4GyUZdi90PXgLZtcGP7EcblQfsUhUTAbz+taNvqg/yvh7brTVdt1xTcpwj8VhDa6QTNxNeYGm+r07yNcwAvuJe670008ZaQ5w/OIhtHdbvDwcEzbdoLnB2vjbpFXxIZoBT8KIOTXxgJS2FM0sjnVTArA0CC67lsXoairPi5YBYOgSwv+EweK5l2QdJBAVEputHqnlWawEXHKvGwTfbwBCa5mcSN/aqTQgLg="),
            1726006523265L),
    CHARACTER_GOKU(SkinCategory.CHARACTER, SkinCollection.DRAGON_BALL, SkinGender.MAN, Skin.library("Goku",
            "ewogICJ0aW1lc3RhbXAiIDogMTcwMzUzOTM5NzkwNSwKICAicHJvZmlsZUlkIiA6ICJiYjUxODk3YWZlYzQ0OWNmYWYzZjgxZmQ0OTA0NzE3MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJhbG9uc29zR0ciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThlMmQ0ZDA0MjE1NDlmYmM4Y2QyNjkwMDQwYTkzZDM4OGE1NmY5MTE1NWViNDUzMjgyZTM1N2E2MzIxMGRiMSIKICAgIH0KICB9Cn0=",
            "WRhND4/s272ZULEh3QFO5CLR0OPYIHDPZ51m3ETZjaVlyoGfn/Bw6H64ZuyP/lbbjTjfyQBR9KKbq5TJVcU9/UqETwq4BLcgO16lVzdLomp5Js9dyUSk/9jakfnTI+YKadvN5u3SNJV3QxsCZK1D7xxfh7P/oP5cBmD1fxhkoSzS5GHulxbprx53f8Dwtj8sHFJ5AXUDudJvqFF5ItG3FDbDMXEaw+Zw9NarzGygn1EGv4bdYuZElupDU3nzEmUUDzmmfiFcFfTMooj5c8bFwuh4GAQgwwxxC9VyZnkijpmVsOA7sFPwYq3/l6i+B3wjrjlTDNWBlngql75pjVS44WM6tPmVTPr0RauN+OvnVobYFT/vQBKlNY3700woemSexfrSv7KHNxlIXKI8Nw/7Y04GfCye1kJIopgMlVqSrVTsvISqeMTZ2w70uN0yo0P6yy3usWB9QbFQ9ZTpYN8+cUoLICluYBe7gI77yFc6qn4xYLsVjUPP/0GsOpctgJ8RI/rvNMHsb+F8WTWrhwblMel7TvgkYu9N3Er3d3PFnnoaj1ZFZTgcFce62KwWgdZa6Rs8SbaH/Zq/dfNcQWziRQcO9HcvuoWphu7JLsXQlBUDo6yinwuXRG+ryU0pn9KjYUK8ZBz2/OqTeOJE5Mfk1f2GAO1p5KZTYU2m9mQljcI="),
            1726006523265L),

    /* Festive Skins */
    FESTIVE_SANTA_CLAUS(SkinCategory.FESTIVE, SkinCollection.CHRISTMAS, SkinGender.MAN, Skin.library("Papai Noel",
            "eyJ0aW1lc3RhbXAiOjE1Nzc0ODUxODc5ODEsInByb2ZpbGVJZCI6ImVkNTNkZDgxNGY5ZDRhM2NiNGViNjUxZGNiYTc3ZTY2IiwicHJvZmlsZU5hbWUiOiJGb3J5eExPTCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgwNGEzZGU1YWU5YjE4YmNiMTMxMmVhNjk5OWM0NDk1ZWYxYmU0MmYxZGFjMWU0Zjc2ZDkxMWE4Y2Y5NjdiYiJ9fX0=",
            "oJ9Z9tLSUk8vp4IbFkSABV3mce8CJvFDRfKkFI0Y/BHugl7REmFjokGpvJDix+K88QSxhZ5Y/b3VjGOZmaiHKcQB2eb3q23kGbEr+hO7x05MiWY+7tbNC5i6TSrGQSKHgsg1VR+rZd7/0BnOHdxjZEOLbScJjCHnLCfyBuh5BhSGbx+8zuguJezKpcG/h4UrXNkiRmyxrlRJ1zUJPdeeryVYp/3r8WYyZ4WUuuMrcg63+vLbqlvPGcZ0T88m+CamZeFuOXUaSRk5U52jdryjQ1mv/1KzsxS4lP9wiCHG2cqWmXJF9EmfB9dvkip4eMfrFTS3bvRxGgUxpWVnGPkIPUcKq8mqv/lfFJuai/R5z7/hShIQy8C4i5M7/bqIzclsW3cYIU2lNtkkP8NzVb2G4npFWtLJr2413y3gHCUfNyYgjXcTRJI+ZXNi0GUDLOiUr5vibDCiDEowl1oTk3hAbRPC4hN2s4iDwoIxZ/G2nzWO/hEAjqJn82atWG1yRpPZvXqYGBDByzzQ+DWlow5ax7m+f1uNZOydADyjVA6SSYk/BOGLacFLfdQ5IXfpp6kQJrWaSToc9/XvLo4g3fHQSg3r7+VxWVAafHC1NrWWu6XTtakSOMV6Ffb1akoPjOsm6fX0dRkLGi6nEqvUDpsD+MiaLjhbN3L/9VqQKFS93Zw="),
            1726006523265L)
    ;

    private final SkinCategory category;
    private final SkinCollection collection;
    private final SkinGender gender;

    private final Skin skin;

    private final long releasedAt;

    private final static List<SkinLibrary> list = new ArrayList<>(Arrays.asList(values()));

    public static Skin of(String name) {
        return list.stream()
                .filter(skin -> skin.getSkin().getDisplayName().equalsIgnoreCase(name))
                .findFirst()
                .map(SkinLibrary::getSkin)
                .orElse(null);
    }

    public static List<Skin> getSkins() {
        return list.stream().map(SkinLibrary::getSkin).collect(Collectors.toList());
    }

    public static List<SkinLibrary> getLibrarySkins(SkinCategory category) {
        return list.stream()
                .filter(skin -> skin.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public static List<Skin> getSkins(SkinCategory category) {
        if (category.equals(SkinCategory.NONE)) return getSkins();

        return list.stream()
                .filter(skin -> skin.getCategory().equals(category))
                .map(SkinLibrary::getSkin)
                .collect(Collectors.toList());
    }

    public static Skin random() {
        return values()[Savage.RANDOM.nextInt(values().length)].getSkin();
    }
}
