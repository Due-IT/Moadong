import React, { useRef, useState, useEffect } from 'react';
import * as Styled from './Banner.styles';
import { BannerProps } from './Banner.styles';

interface BannerComponentProps {
  banners: BannerProps[];
}

const Banner = ({ banners }: BannerComponentProps) => {
  const slideRef = useRef<HTMLDivElement>(null);
  const [currentSlide, setCurrentSlide] = useState(0);
  const IMG_WIDTH = 1180;

  useEffect(() => {
    if (slideRef.current) {
      slideRef.current.style.transform = `translateX(-${currentSlide * IMG_WIDTH}px)`;
    }
  }, [currentSlide]);

  const moveToNextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % banners.length);
  };

  const moveToPrevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + banners.length) % banners.length);
  };

  return (
    <Styled.BannerContainer>
      <Styled.BannerWrapper>
        <Styled.ButtonContainer>
          <Styled.SlideButton onClick={moveToPrevSlide}>
            <img src='/prevButton.png' />
          </Styled.SlideButton>
          <Styled.SlideButton onClick={moveToNextSlide}>
            <img src='/nextButton.png' />
          </Styled.SlideButton>
        </Styled.ButtonContainer>
        <Styled.SlideWrapper ref={slideRef}>
          {banners.map((banner, index) => (
            <Styled.BannerItem key={index}>
              <img
                src={banner.backgroundImage}
                alt={`banner-${index}`}
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover',
                }}
              />
            </Styled.BannerItem>
          ))}
        </Styled.SlideWrapper>
      </Styled.BannerWrapper>
    </Styled.BannerContainer>
  );
};

export default Banner;
